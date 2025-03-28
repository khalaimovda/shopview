package com.github.khalaimovda.shopview.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@SpringBootTest
@Sql(
    scripts = {
        "/data/01_insert_products.sql",
        "/data/02_insert_orders.sql",
        "/data/03_insert_order_product.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(value = "/data/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Transactional
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17.4-alpine"));

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
