package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface OrderProductService {
    Mono<Map<Long, Integer>> getProductIdCountMap(Order order, List<Product> products);
    Mono<Void> addProductToOrder(Order order, Product product);
    Mono<Void> decreaseProductInOrder(Order order, Product product);
    Mono<Void> removeProductFromOrder(Order order, Product product);
}
