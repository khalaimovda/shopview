package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.security.AuthenticatedUser;
import com.github.khalaimovda.shopview.showcase.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
        42L, "testuser", "encodedPassword", List.of()
    );

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<ProductCreateForm> productCreateFormCaptor;

    @Test
    void testGetAllProductsWithUser() throws Exception {
        String search = "Test Search";
        int page = 3;
        int size = 15;
        int total = 20;

        List<ProductListItem> productListItems = generateRandomProducts(15).stream()
            .map(product -> productMapper.toProductListItem(product, "/image/path/1.jpg", 3))
            .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductListItem> resultPage = new PageImpl<>(productListItems, pageable, total);
        when(productService.getAllProducts(anyString(), any(Pageable.class), any(Optional.class)))
            .thenReturn(Mono.just(resultPage));

        StepVerifier
            .create(productController.getAllProducts(model, authenticatedUser, page, size, search))
            .expectNext("products")
            .verifyComplete();

        verify(productService, times(1))
            .getAllProducts(eq(search), pageableCaptor.capture(), eq(Optional.of(authenticatedUser.getId())));
        assertAll(
            () -> assertEquals(pageable.getPageNumber(), pageableCaptor.getValue().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), pageableCaptor.getValue().getPageSize())
        );
        verify(model).addAttribute("page", resultPage);
    }

    @Test
    void testGetAllProductsWithoutUser() throws Exception {
        String search = "Test Search";
        int page = 3;
        int size = 15;
        int total = 20;

        List<ProductListItem> productListItems = generateRandomProducts(15).stream()
            .map(product -> productMapper.toProductListItem(product, "/image/path/1.jpg", 3))
            .toList();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductListItem> resultPage = new PageImpl<>(productListItems, pageable, total);
        when(productService.getAllProducts(anyString(), any(Pageable.class), any(Optional.class)))
            .thenReturn(Mono.just(resultPage));

        StepVerifier
            .create(productController.getAllProducts(model, null, page, size, search))
            .expectNext("products")
            .verifyComplete();

        verify(productService, times(1))
            .getAllProducts(eq(search), pageableCaptor.capture(), eq(Optional.empty()));
        assertAll(
            () -> assertEquals(pageable.getPageNumber(), pageableCaptor.getValue().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), pageableCaptor.getValue().getPageSize())
        );
        verify(model).addAttribute("page", resultPage);
    }


    @Test
    void testCreateProduct() throws Exception {
        String name = "TestName";
        String description = "TestDescription";
        BigDecimal price = new BigDecimal("82.13");
        ProductCreateForm form = new ProductCreateForm();
        form.setName(name);
        form.setDescription(description);
        form.setPrice(price);
        form.setImage(mock(FilePart.class));

        when(productService.createProduct(any(ProductCreateForm.class)))
            .thenReturn(Mono.empty());

        StepVerifier
            .create(productController.createProduct(form))
            .verifyComplete();

        verify(productService, times(1)).createProduct(productCreateFormCaptor.capture());
        assertAll(
            () -> assertEquals(name, productCreateFormCaptor.getValue().getName()),
            () -> assertEquals(description, productCreateFormCaptor.getValue().getDescription()),
            () -> assertEquals(price, productCreateFormCaptor.getValue().getPrice()),
            () -> assertNotNull(productCreateFormCaptor.getValue().getImage())
        );
    }

    @Test
    void testGetProductByIdWithUser() throws Exception {
        ProductDetail productDetail = productMapper.toProductDetail(generateRandomProduct(), "/image/path/1.jpg", 3);
        when(productService.getProductDetailById(anyLong(), any(Optional.class))).thenReturn(Mono.just(productDetail));
        long productId = 13L;

        StepVerifier
            .create(productController.getProductById(model, authenticatedUser, productId))
            .expectNext("product")
            .verifyComplete();

        verify(productService, times(1)).getProductDetailById(productId, Optional.of(authenticatedUser.getId()));
        verify(model).addAttribute("product", productDetail);
    }

    @Test
    void testGetProductByIdWithoutUser() throws Exception {
        ProductDetail productDetail = productMapper.toProductDetail(generateRandomProduct(), "/image/path/1.jpg", 3);
        when(productService.getProductDetailById(anyLong(), any(Optional.class))).thenReturn(Mono.just(productDetail));
        long productId = 13L;

        StepVerifier
            .create(productController.getProductById(model, null, productId))
            .expectNext("product")
            .verifyComplete();

        verify(productService, times(1)).getProductDetailById(productId, Optional.empty());
        verify(model).addAttribute("product", productDetail);
    }
}
