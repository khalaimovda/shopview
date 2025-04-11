package com.github.khalaimovda.shopview.showcase.repository;

import com.github.khalaimovda.shopview.showcase.model.Order;
import com.github.khalaimovda.shopview.showcase.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long>, CustomOrderRepository {
    Mono<User> findByUsername(String username);
}
