package com.github.khalaimovda.shopview.service;

import com.github.khalaimovda.shopview.model.Order;
import com.github.khalaimovda.shopview.model.Product;

import java.util.List;
import java.util.Map;

public interface OrderProductService {
    Map<Long, Integer> getProductIdCountMap(Order order, List<Product> products);
    void addProductToOrder(Order order, Product product);
    void decreaseProductInOrder(Order order, Product product);
    void removeProductFromOrder(Order order, Product product);
}
