package com.github.khalaimovda.shopview.showcase.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebFlux
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

    @BeforeEach
    public void setupDatabase() {
        String product_script = """
            INSERT INTO products(name, description, image_path, price)
            SELECT 'Товар ' || s,
                   'Описание ' || s,
                   'image_path_' || s || '.png',
                   0.99 + s
            FROM generate_series(1, 25) s;
            """;

        String order_script = """
            INSERT INTO orders(is_active)
            SELECT (s = 10)
            FROM generate_series(1, 10) s;
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

        executeScript(product_script).block();
        executeScript(order_script).block();
        executeScript(order_product_script).block();
    }

    @AfterEach
    public void cleanDatabase() {
        String clean_script = """
            DELETE FROM order_product;
            DELETE FROM orders;
            DELETE FROM products;

            ALTER SEQUENCE products_id_seq RESTART WITH 1;
            ALTER SEQUENCE orders_id_seq RESTART WITH 1;
            ALTER SEQUENCE order_product_id_seq RESTART WITH 1;
        """;
        executeScript(clean_script).block();
    }

    private Mono<Void> executeScript(String script) {
        return databaseClient.sql(script).then();
    }

}
