package com.github.khalaimovda.shopview.paymentservice.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {
    ReactiveOAuth2ClientAutoConfiguration.class
})
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17.4-alpine"));

    static {
        postgres.start();
    }

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    protected WebTestClient webTestClient;

    protected final Jwt jwtReadWrite = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claim("resource_access", Map.of(
            "paymentservice", Map.of(
                "roles", List.of("read", "write")
            )
        ))
        .claim("sub", "service-account")
        .build();

    protected final Jwt jwtRead = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claim("resource_access", Map.of(
            "paymentservice", Map.of(
                "roles", List.of("read")
            )
        ))
        .claim("sub", "service-account")
        .build();

    protected final Jwt jwtWrite = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claim("resource_access", Map.of(
            "paymentservice", Map.of(
                "roles", List.of("write")
            )
        ))
        .claim("sub", "service-account")
        .build();

    protected final Jwt jwtNoRoles = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claim("resource_access", Map.of(
            "paymentservice", Map.of(
                "roles", List.of()
            )
        ))
        .claim("sub", "service-account")
        .build();

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
            postgres.getHost(),
            postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgres.getDatabaseName());

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.liquibase.contexts", () -> "test"); // It is to create users table
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @BeforeEach
    public void setupDatabase() {
        String user_script = """
            INSERT INTO users(username, password, roles)
            VALUES ('ordinary', '$2a$10$pY6ODS4d.d3703hEN0Gv5O0PXS0tNEEgN/TjTDiz6ZVkGUMREJvq.', '{}');
            """;

        String balance_script = """
            INSERT INTO balance(user_id, balance)
            SELECT id, 300.0 FROM users where username = 'ordinary';
            """;

        executeScript(user_script).block();
        executeScript(balance_script).block();
    }

    @AfterEach
    public void cleanDatabase() {
        String clean_script = """
            DELETE FROM balance;
            DELETE FROM users;

            ALTER SEQUENCE balance_id_seq RESTART WITH 1;
            ALTER SEQUENCE users_id_seq RESTART WITH 1;
        """;
        executeScript(clean_script).block();
    }

    private Mono<Void> executeScript(String script) {
        return databaseClient.sql(script)
            .fetch()
            .rowsUpdated()
            .then();
    }

    protected JwtAuthenticationToken createToken(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> paymentservice = (Map<String, Object>) resourceAccess.get("paymentservice");
        List<String> roles = (List<String>) paymentservice.get("roles");
         return new JwtAuthenticationToken(
            jwt, roles.stream().map(SimpleGrantedAuthority::new).toList(),
            "service-account"
        );
    }
}
