package com.github.khalaimovda.shopview.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.khalaimovda.shopview.utils.ImageUtils.createRandomBytes;


@SpringBootTest
@Sql(
    scripts = {
        "/data/01_insert_products.sql",
        "/data/02_insert_orders.sql",
        "/data/03_insert_order_product.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(value = "/data/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
//@Transactional
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17.4-alpine"));

    static {
        postgres.start();
    }

    static Path tempImageDir;


    @BeforeEach
    public void saveImages(@Autowired Environment env) throws IOException {
        String uploadDir = env.getProperty("app.images.upload-dir");
        for (int i = 1; i <= 25; i++) {
            String fileName = "image_path_" + i + ".png";
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            byte[] imageBytes = createRandomBytes();
            InputStream imageInputStream = new ByteArrayInputStream(imageBytes);
            Files.copy(imageInputStream, filePath);
        }
    }


    @DynamicPropertySource
    static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @DynamicPropertySource
    static void configureImageServiceProperties(DynamicPropertyRegistry registry) throws IOException {
        tempImageDir = Files.createTempDirectory("test-images");
        registry.add("app.images.upload-dir", () -> tempImageDir.toString());
    }

    @AfterEach
    public void cleanUpTempImageDir() throws IOException {
        Files.list(tempImageDir).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @AfterAll
    public static void deleteTempDir() throws IOException {
        Files.deleteIfExists(tempImageDir);
    }
}
