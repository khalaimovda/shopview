package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.ProductListItem;
import com.github.khalaimovda.shopview.showcase.mapper.ProductMapper;
import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.Product;
import com.github.khalaimovda.shopview.showcase.repository.OrderRepository;
import com.github.khalaimovda.shopview.showcase.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static com.github.khalaimovda.shopview.showcase.utils.OrderUtils.generateRandomActiveOrder;
import static com.github.khalaimovda.shopview.showcase.utils.ProductUtils.generateRandomProducts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCacheServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Mock
    private ImageService imageService;

    @Mock
    private OrderProductService orderProductService;

    @InjectMocks
    private ProductCacheServiceImpl productCacheService;

    @Captor
    private ArgumentCaptor<List<Long>> productIdsCaptor;

    @Test
    void testCountProducts() {
        String name = "Test name";
        String description = "Test description";
        int totalElements = 15;
        when(productRepository.countByNameOrDescriptionContaining(anyString(), anyString()))
            .thenReturn(Mono.just(totalElements));

        StepVerifier
            .create(productCacheService.countProducts(name, description))
            .assertNext(count -> assertEquals(totalElements, count))
            .verifyComplete();

        verify(productRepository, times(1))
            .countByNameOrDescriptionContaining(eq(name), eq(description));
    }

    @Test
    void testGetAllProducts() {
        // Arrange
        String name = "Test name";
        String description = "Test description";
        String imageServicePostfix = "/path";

        int limit = 3;
        long offset = 0L;
        List<Product> products = generateRandomProducts(3);

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
        Mono<List<ProductListItem>> monoProductPage = productCacheService.getProductItems(name, description, limit, offset);

        // Assert
        StepVerifier
            .create(monoProductPage)
            .assertNext(productListItems -> assertEquals(expectedResultProducts, productListItems))
            .verifyComplete();

        verify(productRepository, times(1))
            .findByNameOrDescriptionContaining(eq(name), eq(description), eq(limit), eq(offset));

        verify(orderRepository, times(1))
            .findByIsActiveTrue();

        verify(orderProductService, times(1))
            .getProductIdCountMap(eq(activeOrder.getId()), productIdsCaptor.capture());
        assertEquals(products.stream().map(Product::getId).toList(), productIdsCaptor.getValue());

        verify(imageService, times(3)).getImageSrcPath(anyString());
    }

}
