package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.model.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> createUser(UserRegistrationForm form);
}
