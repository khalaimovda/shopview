package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductDetail;
import com.github.khalaimovda.shopview.dto.ProductListItem;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.khalaimovda.shopview.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.*;
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

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Mock
    private ImageService imageService;

    @Mock
    private OrderProductService orderProductService;

    @Mock
    private ImageRollbackService imageRollbackService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<MockMultipartFile> imageFileCaptor;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

//    @Test
//    void testCreateProduct() {
//        // Arrange
//        MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[100]);
//        ProductCreateForm form = ProductCreateForm.builder()
//            .name("TestName")
//            .description("TestDescription")
//            .image(imageFile)
//            .price(new BigDecimal("82.13"))
//            .build();
//        when(imageService.saveImage(any())).thenReturn("image_path");
//
//        // Act
//        productService.createProduct(form);
//
//        // Assert
//        verify(imageService, times(1)).saveImage(imageFileCaptor.capture());
//        assertEquals(imageFile, imageFileCaptor.getValue());
//
//        verify(imageRollbackService, times(1)).registerImageRollback("image_path");
//
//        verify(productRepository, times(1)).save(productCaptor.capture());
//        assertAll(
//            "Check Product fields",
//            () -> assertEquals(form.getName(), productCaptor.getValue().getName()),
//            () -> assertEquals(form.getDescription(), productCaptor.getValue().getDescription()),
//            () -> assertEquals("image_path", productCaptor.getValue().getImagePath()),
//            () -> assertEquals(form.getPrice(), productCaptor.getValue().getPrice())
//        );
//    }

    @Test
    void testGetAllProducts() {
        // Arrange
        String contentSubstring = "Test search";
        int totalElements = 15;
        String imageServicePostfix = "/path";

        Pageable pageable = PageRequest.of(0, 3);
        List<Product> products = generateRandomProducts(3);
        Page<Product> page =  new PageImpl<>(products, pageable, totalElements);
        when(productRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            anyString(), anyString(), any())
        ).thenReturn(page);

        Order activeOrder = generateRandomActiveOrder(List.of(products.getFirst()));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeOrder));

        Integer count = activeOrder.getOrderProducts().getFirst().getCount();
        Map<Long, Integer> productIdCountMap = Map.of(products.getFirst().getId(), count);
        when(orderProductService.getProductIdCountMap(any(), any())).thenReturn(productIdCountMap);

        when(imageService.getImageSrcPath(anyString())).thenAnswer(invocation -> {
            String imagePath = invocation.getArgument(0);
            return imagePath + imageServicePostfix;
        });

        List<ProductListItem> expectedResultProducts = products.stream().map(
            product -> productMapper.toProductListItem(
                product,
                product.getImagePath() + imageServicePostfix,
                productIdCountMap.getOrDefault(product.getId(), 0)
            )
        ).toList();


        // Act
        Page<ProductListItem> resultProductPage = productService.getAllProducts(contentSubstring, pageable);

        // Assert
        verify(productRepository, times(1))
            .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq(contentSubstring),
                eq(contentSubstring),
                pageableCaptor.capture()
            );
        assertAll(
            "Check pageable argument",
            () -> assertEquals(pageable.getPageNumber(), pageableCaptor.getValue().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), pageableCaptor.getValue().getPageSize())
        );

        verify(imageService, times(3)).getImageSrcPath(anyString());

        assertEquals(expectedResultProducts, resultProductPage.getContent());
        assertAll(
            () -> assertEquals(pageable.getPageNumber(), resultProductPage.getPageable().getPageNumber()),
            () -> assertEquals(pageable.getPageSize(), resultProductPage.getPageable().getPageSize())
        );
        assertEquals(totalElements, resultProductPage.getTotalElements());
    }

    @Test
    void testGetProductByIdNotFound() {
        long productId = 135L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> productService.getProductById(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById() {
        // Arrange
        String imageServicePostfix = "/path";
        Product product = generateRandomProduct();

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        Order activeOrder = generateRandomActiveOrder(List.of(product));
        when(orderRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeOrder));

        OrderProduct orderProduct = activeOrder.getOrderProducts().getFirst();
        Integer count = orderProduct.getCount();
        when(orderProductRepository.findById(any())).thenReturn(Optional.of(orderProduct));

        when(imageService.getImageSrcPath(anyString())).thenAnswer(invocation -> {
            String imagePath = invocation.getArgument(0);
            return imagePath + imageServicePostfix;
        });

        ProductDetail expectedProductDetail = productMapper.toProductDetail(
            product, product.getImagePath() + imageServicePostfix, count);

        // Act
        ProductDetail productDetail = productService.getProductById(product.getId());

        // Assert
        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderProductRepository, times(1)).findById(any());
        verify(imageService, times(1)).getImageSrcPath(product.getImagePath());
        assertEquals(expectedProductDetail, productDetail);
    }
}
