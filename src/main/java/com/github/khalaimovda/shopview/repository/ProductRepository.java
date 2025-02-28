package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // todo: независимо от регистра
    Page<Product> findAllByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);
}
