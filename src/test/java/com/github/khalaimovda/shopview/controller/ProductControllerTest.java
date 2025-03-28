package com.github.khalaimovda.shopview.controller;


import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.dto.ProductListItem;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        when(productService.getAllProducts(any(), any())).thenReturn(
            new PageImpl<>(productListItems, pageable, total)
        );

        mockMvc.perform(get("/products")
                .param("search", search)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("products"));

        verify(productService, times(1)).getAllProducts(eq(search), pageableCaptor.capture());
        assertAll(
            () -> assertEquals(pageable.getPageNumber(), pageableCaptor.getValue().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), pageableCaptor.getValue().getPageSize())
        );
    }


    @Test
    void testCreateProduct() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[100]);
        ProductCreateForm form = ProductCreateForm.builder()
            .name("TestName")
            .description("TestDescription")
            .image(imageFile)
            .price(new BigDecimal("82.13"))
            .build();

        mockMvc.perform(multipart("/products")
                .file("image", form.getImage().getBytes())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .formField("name", form.getName())
                .formField("description", form.getDescription())
                .formField("price", String.valueOf(form.getPrice())))
            .andExpect(status().isCreated());

        verify(productService, times(1)).createProduct(productCreateFormCaptor.capture());
        assertAll(
            () -> assertEquals(form.getName(), productCreateFormCaptor.getValue().getName()),
            () -> assertEquals(form.getDescription(), productCreateFormCaptor.getValue().getDescription()),
            () -> assertEquals(form.getPrice(), productCreateFormCaptor.getValue().getPrice()),
            () -> assertEquals(form.getImage().getBytes(), productCreateFormCaptor.getValue().getImage().getBytes())
        );
    }

    @Test
    void testGetProductById() throws Exception {
        ProductDetail productDetail = productMapper.toProductDetail(generateRandomProduct(), "/image/path/1.jpg", 3);
        when(productService.getProductById(anyLong())).thenReturn(productDetail);
        long productId = 13L;

        mockMvc.perform(get("/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("product"));

        verify(productService, times(1)).getProductById(productId);
    }
}
