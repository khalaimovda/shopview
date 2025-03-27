package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ImageService imageService;

    @Mock
    private OrderProductService orderProductService;

    @Mock
    private ImageRollbackService imageRollbackService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testCreateProduct() {
        ProductCreateForm form = ProductCreateForm.builder()
            .name("TestName")
            .description("TestDescription")
            .image(new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[100]
            ))
            .price(new BigDecimal("82.13"))
            .build();

        when(imageService.saveImage(any())).thenReturn("image_path");
        doNothing().when(imageRollbackService).registerImageRollback(any());
        productService.createProduct(form);

        verify(imageService, times(1)).saveImage(any());
    }
}