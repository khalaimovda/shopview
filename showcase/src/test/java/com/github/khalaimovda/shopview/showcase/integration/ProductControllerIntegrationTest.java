package com.github.khalaimovda.shopview.showcase.integration;

import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.github.khalaimovda.shopview.showcase.utils.ImageUtils.createRandomBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private OrderRepository orderRepository;

    @Test
    public void testGetAllProducts() {
        webTestClient.get()
            .uri(builder -> builder
                .path("/products")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);

                // Check that the pagination container is present
                assertTrue(body.contains("class=\"pagination-container\""));
                // Check that at least one product block is present
                assertTrue(body.contains("class=\"product\""));

                // Check specific values for the first product (from the end) (s = 25)
                assertTrue(body.contains("Товар 25"));
                assertTrue(body.contains("25.99")); // Price: 0.99 + 25 = 25.99
                assertTrue(body.contains("src=\"/images/image_path_25.png\""));

                // Check specific values for the second product (from the end) (s = 24)
                assertTrue(body.contains("Товар 24"));
                assertTrue(body.contains("24.99")); // Price: 0.99 + 24 = 24.99
                assertTrue(body.contains("src=\"/images/image_path_24.png\""));

                // Check specific values for the last product on the page (25 - 20 + 1 = 6) (s = 6)
                assertTrue(body.contains("Товар 6"));
                assertTrue(body.contains("6.99")); // Price: 0.99 + 6 = 6.99
                assertTrue(body.contains("src=\"/images/image_path_6.png\""));
            });
    }

    @Test
    public void testGetAllProductsCached() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            webTestClient.get()
                .uri(builder -> builder
                    .path("/products")
                    .queryParam("page", "0")
                    .queryParam("size", "20")
                    .queryParam("search", "searchValue")
                    .build())
                .exchange()
                .expectStatus().isOk();
        }

        verify(productRepository, times(1))
            .countByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"));
        verify(productRepository, times(1))
            .findByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"), eq(20), eq(0L));
        verify(orderRepository, times(1)).findByIsActiveTrue();

        // Wait until TTL is expired
        TimeUnit.SECONDS.sleep(2L);

        webTestClient.get()
            .uri(builder -> builder
                .path("/products")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("search", "searchValue")
                .build())
            .exchange()
            .expectStatus().isOk();

        verify(productRepository, times(2))
            .countByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"));
        verify(productRepository, times(2))
            .findByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"), eq(20), eq(0L));
        verify(orderRepository, times(2)).findByIsActiveTrue();
    }


    @Test
    void testCreateProduct() {
        String productName = "TestName";
        String productDescription = "TestDescription";
        String imageName = "test-create.jpg";
        BigDecimal productPrice = new BigDecimal("82.13");

        webTestClient.post()
            .uri("/products")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("name", productName)
                .with("description", productDescription)
                .with("price", productPrice.toString())
                .with("image", new HttpEntity<>(
                    new ByteArrayResource(createRandomBytes()) {
                        @Override
                        public String getFilename() {
                            return imageName;
                        }
                    })))
            .exchange()
            .expectStatus().isCreated();

        StepVerifier
            .create(productRepository.findByName(productName))
            .assertNext(
                product -> assertAll(
                    () -> assertEquals(productName, product.getName()),
                    () -> assertEquals(productDescription, product.getDescription()),
                    () -> assertTrue(product.getImagePath().endsWith(imageName)),
                    () -> assertEquals(productPrice, product.getPrice())
                )
            )
            .verifyComplete();
    }


    @Test
    void testCreateProductInvalidCache() {
        String productName = "TestName";
        String productDescription = "TestDescription";
        String imageName = "test-create.jpg";
        BigDecimal productPrice = new BigDecimal("82.13");

        webTestClient.get()
            .uri(builder -> builder
                .path("/products")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("search", "searchValue")
                .build())
            .exchange()
            .expectStatus().isOk();

        verify(productRepository, times(1))
            .countByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"));
        verify(productRepository, times(1))
            .findByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"), eq(20), eq(0L));
        verify(orderRepository, times(1)).findByIsActiveTrue();

        // Create post
        //////////////////////////////////////////////////////////////////////////////
        webTestClient.post()
            .uri("/products")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("name", productName)
                .with("description", productDescription)
                .with("price", productPrice.toString())
                .with("image", new HttpEntity<>(
                    new ByteArrayResource(createRandomBytes()) {
                        @Override
                        public String getFilename() {
                            return imageName;
                        }
                    })))
            .exchange()
            .expectStatus().isCreated();
        // After post creation cache must be invalidated
        //////////////////////////////////////////////////////////////////////////////

        webTestClient.get()
            .uri(builder -> builder
                .path("/products")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("search", "searchValue")
                .build())
            .exchange()
            .expectStatus().isOk();

        verify(productRepository, times(2))
            .countByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"));
        verify(productRepository, times(2))
            .findByNameOrDescriptionContaining(eq("searchValue"), eq("searchValue"), eq(20), eq(0L));
        verify(orderRepository, times(2)).findByIsActiveTrue();
    }

    @Test
    void testGetProductById() {
        // Based on predefined sql
        ProductDetail expectedProductDetail = new ProductDetail();
        expectedProductDetail.setId(1L);
        expectedProductDetail.setName("Товар 1");
        expectedProductDetail.setDescription("Описание 1");
        expectedProductDetail.setImagePath("/images/image_path_1.png");
        expectedProductDetail.setPrice(new BigDecimal("1.99"));
        expectedProductDetail.setCount(0); // First product is not in cart

        webTestClient.get()
            .uri("/products/" + expectedProductDetail.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);

                assertTrue(body.contains(expectedProductDetail.getName()));
                assertTrue(body.contains(expectedProductDetail.getDescription()));
                assertTrue(body.contains(expectedProductDetail.getImagePath()));
                assertTrue(body.contains(expectedProductDetail.getPrice().toString()));
                assertTrue(body.contains(String.valueOf(expectedProductDetail.getCount())));
            });
    }

    @Test
    void testGetProductByIdCached() throws InterruptedException {
        long productId = 1L;

        for (int i = 0; i < 15; i++) {
            webTestClient.get()
                .uri("/products/" + productId)
                .exchange()
                .expectStatus().isOk();
        }

        verify(productRepository, times(1)).findById(productId);
        verify(orderRepository, times(1)).findByIsActiveTrue();

        // Wait until TTL is expired
        TimeUnit.SECONDS.sleep(2L);

        webTestClient.get()
            .uri("/products/" + productId)
            .exchange()
            .expectStatus().isOk();

        verify(productRepository, times(2)).findById(productId);
        verify(orderRepository, times(2)).findByIsActiveTrue();
    }
}
