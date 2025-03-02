package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.dto.ProductCreateForm;
import com.github.khalaimovda.shopview.dto.ProductListResponseDto;
import com.github.khalaimovda.shopview.dto.ProductResponseDto;
import com.github.khalaimovda.shopview.exception.ProductCreationException;
import com.github.khalaimovda.shopview.mapper.ProductMapper;
import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.OrderProduct;
import com.github.khalaimovda.shopview.model.OrderProductId;
import com.github.khalaimovda.shopview.model.Product;
import com.github.khalaimovda.shopview.repository.OrderProductRepository;
import com.github.khalaimovda.shopview.repository.OrderRepository;
import com.github.khalaimovda.shopview.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    // todo: ActiveOrder нужно закэшировать!

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductMapper productMapper;
    private final ImageService imageService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponseDto> getAllProducts(String contentSubstring, Pageable pageable) {

        Page<Product> productPage = productRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            contentSubstring, contentSubstring, pageable);

        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue();
        Map<Long, Integer> productIdCountMap = activeOrder.map(order -> {
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderAndProductIn(
                activeOrder.get(), productPage.getContent()
            );
            return orderProducts.stream()
                .collect(Collectors.toMap(op -> op.getId().getProductId(), op -> op.getCount()));
        }).orElseGet(HashMap::new);

        List<ProductListResponseDto> resultProducts = productPage.getContent().stream()
            .map(product -> {
                Integer count = productIdCountMap.getOrDefault(product.getId(), 0);
                String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());
                return productMapper.toProductListResponseDto(product,  imageSrcPath, count);
            }).toList();

        return new PageImpl<>(resultProducts, productPage.getPageable(), productPage.getTotalElements());
    }

    @Transactional
    @Override
    public void createProduct(ProductCreateForm form) {
        String imagePath;
        try {
            imagePath = imageService.saveImage(form.getImage());
        } catch (Exception e) {
            throw new ProductCreationException("Error saving product image", e);
        }

        registerImageRollback(imagePath);
        Product product = productMapper.toProduct(form, imagePath);

        try {
            productRepository.save(product);
        } catch (Exception e) {
            throw new ProductCreationException("Error saving product to database", e);
        }
    }

    @Override
    public ProductResponseDto getProductById(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new NoSuchElementException(String.format("Product with id %s not found", id));
        }
        Product product = optionalProduct.get();

        Optional<Order> activeOrder = orderRepository.findByIsActiveTrue();
        Integer count = activeOrder.map(order -> {
            Optional<OrderProduct> orderProduct = orderProductRepository.findById(new OrderProductId(order.getId(), product.getId()));
            return orderProduct.map(OrderProduct::getCount).orElse(0);
        }).orElse(0);
        String imageSrcPath = imageService.getImageSrcPath(product.getImagePath());

        return productMapper.toProductResponseDto(product, imageSrcPath, count);
    }

    /**
     * Register TransactionSynchronization which will remove image if transaction is rolled back
     */
    private void registerImageRollback(String imagePath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    imageService.deleteImage(imagePath);
                }
            }
            @Override public void suspend() {}
            @Override public void resume() {}
            @Override public void flush() {}
            @Override public void beforeCommit(boolean readOnly) {}
            @Override public void beforeCompletion() {}
        });
    }
}
