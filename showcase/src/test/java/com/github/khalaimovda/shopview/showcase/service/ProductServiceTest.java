package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.showcase.dto.ProductDetail;
import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.OrderProduct;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.khalaimovda.shopview.showcase.utils.OrderProductUtils.generateRandomOrderProduct;
import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProduct;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    FilePart imageFile;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<FilePart> imageFileCaptor;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Captor
    private ArgumentCaptor<List<Long>> productIdsCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void testCreateProduct() {
        // Arrange
        ProductCreateForm form = ProductCreateForm.builder()
            .name("TestName")
            .description("TestDescription")
            .image(imageFile)
            .price(new BigDecimal("82.13"))
            .build();
        when(imageService.saveImage(any(FilePart.class))).thenReturn(Mono.just("image_path"));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(new Product()));

        // Act
        Mono<Void> monoResult = productService.createProduct(form);

        // Assert
        StepVerifier.create(monoResult).verifyComplete();

        verify(imageService, times(1)).saveImage(imageFileCaptor.capture());
        assertEquals(imageFile, imageFileCaptor.getValue());

        verify(productRepository, times(1)).save(productCaptor.capture());
        assertAll(
            "Check Product fields",
            () -> assertEquals(form.getName(), productCaptor.getValue().getName()),
            () -> assertEquals(form.getDescription(), productCaptor.getValue().getDescription()),
            () -> assertEquals("image_path", productCaptor.getValue().getImagePath()),
            () -> assertEquals(form.getPrice(), productCaptor.getValue().getPrice())
        );
    }


    @Test
    void testGetAllProducts() {
        // Arrange
        String contentSubstring = "Test search";
        long totalElements = 15L;
        String imageServicePostfix = "/path";

        Pageable pageable = PageRequest.of(0, 3);
        List<Product> products = generateRandomProducts(3);
        Page<Product> page =  new PageImpl<>(products, pageable, totalElements);

        when(productRepository.countByNameOrDescriptionContaining(anyString(), anyString()))
            .thenReturn(Mono.just(totalElements));

        when(productRepository.findByNameOrDescriptionContaining(anyString(), anyString(), anyInt(), anyLong()))
            .thenReturn(Flux.just(products.toArray(new Product[0])));

        Order activeOrder = generateRandomActiveOrder();
        when(orderRepository.findByIsActiveTrue())
            .thenReturn(Mono.just(activeOrder));

        Integer firstProductCount = 4;
        Map<Long, Integer> productIdCountMap = Map.of(products.getFirst().getId(), firstProductCount);
        when(orderProductService.getProductIdCountMap(anyLong(), anyList()))
            .thenReturn(Mono.just(productIdCountMap));

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
        Mono<Page<ProductListItem>> monoProductPage = productService.getAllProducts(contentSubstring, pageable);

        // Assert
        StepVerifier
            .create(monoProductPage)
            .assertNext(resultPage -> assertAll(
                () -> assertEquals(pageable.getPageNumber(), resultPage.getPageable().getPageNumber()),
                () -> assertEquals(pageable.getPageSize(), resultPage.getPageable().getPageSize()),
                () -> assertEquals(totalElements, resultPage.getTotalElements()),
                () -> assertEquals(expectedResultProducts, resultPage.getContent())
            ))
            .verifyComplete();

        verify(productRepository, times(1))
            .countByNameOrDescriptionContaining(eq(contentSubstring), eq(contentSubstring));

        verify(productRepository, times(1))
            .findByNameOrDescriptionContaining(eq(contentSubstring), eq(contentSubstring), eq(pageable.getPageSize()), eq(pageable.getOffset()));

        verify(orderRepository, times(1))
            .findByIsActiveTrue();

        verify(orderProductService, times(1))
            .getProductIdCountMap(eq(activeOrder.getId()), productIdsCaptor.capture());
        assertEquals(products.stream().map(Product::getId).toList(), productIdsCaptor.getValue());

        verify(imageService, times(3)).getImageSrcPath(anyString());
    }

    @Test
    void testGetProductByIdNotFound() {
        long productId = 135L;
        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        Mono<ProductDetail> monoProduct = productService.getProductById(productId);

        StepVerifier
            .create(monoProduct)
            .expectError(NoSuchElementException.class)
            .verify();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void testGetProductById() {
        // Arrange
        String imageServicePostfix = "/path";
        Product product = generateRandomProduct();

        when(productRepository.findById(anyLong()))
            .thenReturn(Mono.just(product));

        Order activeOrder = generateRandomActiveOrder();
        when(orderRepository.findByIsActiveTrue())
            .thenReturn(Mono.just(activeOrder));

        OrderProduct orderProduct = generateRandomOrderProduct(activeOrder.getId(), product.getId());
        Integer count = orderProduct.getCount();
        when(orderProductRepository.findByOrderIdAndProductId(anyLong(), anyLong()))
            .thenReturn(Mono.just(orderProduct));

        when(imageService.getImageSrcPath(anyString())).thenAnswer(invocation -> {
            String imagePath = invocation.getArgument(0);
            return imagePath + imageServicePostfix;
        });

        ProductDetail expectedProductDetail = productMapper.toProductDetail(
            product, product.getImagePath() + imageServicePostfix, count);

        // Act
        Mono<ProductDetail> monoProductDetail = productService.getProductById(product.getId());

        // Assert
        StepVerifier
            .create(monoProductDetail)
            .assertNext(productDetail -> assertEquals(expectedProductDetail, productDetail))
            .verifyComplete();

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).findByIsActiveTrue();
        verify(orderProductRepository, times(1)).findByOrderIdAndProductId(anyLong(), anyLong());
        verify(imageService, times(1)).getImageSrcPath(product.getImagePath());
    }
}
