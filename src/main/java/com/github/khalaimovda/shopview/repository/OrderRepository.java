package com.github.khalaimovda.shopview.repository;

import com.github.khalaimovda.shopview.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIsActiveTrue();

    @EntityGraph(attributePaths = "orderProducts.product")
    List<Order> findAllByIsActiveFalseOrderByIdDesc();
}
