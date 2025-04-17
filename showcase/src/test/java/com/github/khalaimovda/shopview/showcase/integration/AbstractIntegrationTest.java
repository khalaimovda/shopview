package com.github.khalaimovda.shopview.showcase.integration;

import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {
    ReactiveOAuth2ClientAutoConfiguration.class
})
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17.4-alpine"));

    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7.4.2-alpine"));

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    private ReactiveOAuth2AuthorizedClientManager mockManager;

    protected final AuthenticatedUser adminUser = new AuthenticatedUser(
        1L,
        "admin",
        "password",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );

    protected final AuthenticatedUser ordinaryUser = new AuthenticatedUser(
        2L,
        "ordinary",
        "password",
        List.of()
    );

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
            postgres.getHost(),
            postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgres.getDatabaseName());

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @DynamicPropertySource
    static void configureRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getRedisPort);
    }

    @DynamicPropertySource
    static void configureCacheProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cache.redis.time-to-live", () -> "PT2S");
    }

    @BeforeEach
    public void setupDatabase() {
        String user_script = """
            INSERT INTO users(username, password, roles)
            VALUES ('admin', '$2a$10$pY6ODS4d.d3703hEN0Gv5O0PXS0tNEEgN/TjTDiz6ZVkGUMREJvq.', '{"ADMIN"}');
            
            INSERT INTO users(username, password, roles)
            VALUES ('ordinary', '$2a$10$pY6ODS4d.d3703hEN0Gv5O0PXS0tNEEgN/TjTDiz6ZVkGUMREJvq.', '{}');
            """;

        String product_script = """
            INSERT INTO products(name, description, image_path, price)
            SELECT 'Товар ' || s,
                   'Описание ' || s,
                   'image_path_' || s || '.png',
                   0.99 + s
            FROM generate_series(1, 25) s;
            """;

        String order_script = """
            INSERT INTO orders (is_active, user_id)
            SELECT (s = 10), u.id
            FROM generate_series(1, 10) s,
                 (SELECT id FROM users WHERE username = 'ordinary') u;
            """;

        String order_product_script = """
            WITH numbered_orders AS (
                SELECT id, row_number() OVER (ORDER BY id) AS rn
                FROM orders
            )
            INSERT INTO order_product(order_id, product_id, count)
            SELECT o.id, p.id, (o.rn + pc) AS count
            FROM numbered_orders o
            CROSS JOIN generate_series(0, 3) AS pc
            JOIN products p ON p.name = 'Товар ' || (o.rn + pc + 3);
            """;

        executeScript(user_script).block();
        executeScript(product_script).block();
        executeScript(order_script).block();
        executeScript(order_product_script).block();
    }

    @BeforeEach
    public void cleanRedisCache() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @AfterEach
    public void cleanDatabase() {
        String clean_script = """
            DELETE FROM order_product;
            DELETE FROM orders;
            DELETE FROM users;
            DELETE FROM products;

            ALTER SEQUENCE users_id_seq RESTART WITH 1;
            ALTER SEQUENCE products_id_seq RESTART WITH 1;
            ALTER SEQUENCE orders_id_seq RESTART WITH 1;
            ALTER SEQUENCE order_product_id_seq RESTART WITH 1;
        """;
        executeScript(clean_script).block();
    }

    private Mono<Void> executeScript(String script) {
        return databaseClient.sql(script)
            .fetch()
            .rowsUpdated()
            .then();
    }

    protected Authentication createAuthentication(AuthenticatedUser user) {
         return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }
}
