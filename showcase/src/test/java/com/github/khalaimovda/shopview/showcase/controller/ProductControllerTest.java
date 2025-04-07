package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static com.github.khalaimovda.shopview.showcase.utils.ImageUtils.createRandomBytes;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@WebFluxTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductService productService;

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<ProductCreateForm> productCreateFormCaptor;

    @Test
    void testGetAllProducts() throws Exception {
        String search = "Test Search";
        int page = 3;
        int size = 15;
        int total = 20;

        List<ProductListItem> productListItems = generateRandomProducts(15).stream()
            .map(product -> productMapper.toProductListItem(product, "/image/path/1.jpg", 3))
            .toList();
        Pageable pageable = PageRequest.of(page, size);
        when(productService.getAllProducts(anyString(), any(Pageable.class)))
            .thenReturn(Mono.just(new PageImpl<>(productListItems, pageable, total)));

        webTestClient.get()
            .uri(builder -> builder
                .path("/products")
                .queryParam("search", search)
                .queryParam("page", page)
                .queryParam("size", size)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("products"));
            });

        verify(productService, times(1)).getAllProducts(eq(search), pageableCaptor.capture());
        assertAll(
            () -> assertEquals(pageable.getPageNumber(), pageableCaptor.getValue().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), pageableCaptor.getValue().getPageSize())
        );
    }


    @Test
    void testCreateProduct() throws Exception {
        String name = "TestName";
        String description = "TestDescription";
        BigDecimal price = new BigDecimal("82.13");

        when(productService.createProduct(any(ProductCreateForm.class)))
            .thenReturn(Mono.empty());

        webTestClient.post()
            .uri("/products")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("name", name)
                .with("description", description)
                .with("price", price.toString())
                .with("image", new HttpEntity<>(
                    new ByteArrayResource(createRandomBytes()) {
                        @Override
                        public String getFilename() {
                            return "test-image.jpg";
                        }
                    })))
            .exchange()
            .expectStatus().isCreated();

        verify(productService, times(1)).createProduct(productCreateFormCaptor.capture());
        assertAll(
            () -> assertEquals(name, productCreateFormCaptor.getValue().getName()),
            () -> assertEquals(description, productCreateFormCaptor.getValue().getDescription()),
            () -> assertEquals(price, productCreateFormCaptor.getValue().getPrice()),
            () -> assertNotNull(productCreateFormCaptor.getValue().getImage())
        );
    }

    @Test
    void testGetProductById() throws Exception {
        ProductDetail productDetail = productMapper.toProductDetail(generateRandomProduct(), "/image/path/1.jpg", 3);
        when(productService.getProductById(anyLong())).thenReturn(Mono.just(productDetail));
        long productId = 13L;

        webTestClient.get()
            .uri("/products/" + productId)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("product"));
            });

        verify(productService, times(1)).getProductById(productId);
    }
}
