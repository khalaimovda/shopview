package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name,
        String description,
        Pageable pageable
    );

    Optional<Product> findByName(String name);
}
