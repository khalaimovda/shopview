package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE
          LOWER(name) LIKE LOWER(:name) AND
          LOWER(description) LIKE LOWER(:description);
    """)
    Mono<Long> countByNameOrDescriptionContaining(String name, String description);

    @Query("""
        SELECT * FROM products
        WHERE
          LOWER(name) LIKE LOWER(:name) AND
          LOWER(description) LIKE LOWER(:description)
        LIMIT :limit
        OFFSET :offset"
    """)
    Flux<Product> findByNameOrDescriptionContaining(String name, String description, int limit, long offset);

    Mono<Product> findByName(String name);
}
