package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.UserListItem;
import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {
    Mono<User> createUser(UserRegistrationForm form);
    Mono<List<UserListItem>> getAllUsers();
}
