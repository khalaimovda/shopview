package com.github.khalaimovda.shopview.showcase.utils;

import com.github.khalaimovda.shopview.showcase.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ProductUtils {

    private static final Random random = new Random();

    public static Product generateRandomProduct() {
        Product product = new Product();
        product.setId(random.nextLong(1, Long.MAX_VALUE));
        product.setName("Test Name " + product.getId());
        product.setDescription("Test Description " + product.getId());
        product.setImagePath("path/to/image/" + product.getId());
        product.setPrice(BigDecimal.valueOf(random.nextDouble(10.0, 100.0)));
        return product;
    }

    public static List<Product> generateRandomProducts(int count) {
        return Stream.generate(ProductUtils::generateRandomProduct).limit(count).toList();
    }
}
