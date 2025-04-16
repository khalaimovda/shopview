package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.UserListItem;
import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.mapper.UserMapper;
import com.github.khalaimovda.shopview.showcase.model.User;
import com.github.khalaimovda.shopview.showcase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PaymentService paymentService;

    @Value("${app.payment-service.default-balance}")
    private BigDecimal defaultBalance;

    @Override
    @Secured("ROLE_ADMIN")
    public Mono<User> createUser(UserRegistrationForm form) {
        User user = userMapper.toUser(form, passwordEncoder);
        return userRepository
            .save(user)
            .flatMap(createdUser -> paymentService
                .addBalance(createdUser.getId(), defaultBalance)
                .thenReturn(createdUser));
    }

    @Override
    @Secured("ROLE_ADMIN")
    public Mono<List<UserListItem>> getAllUsers() {
        return userRepository
            .findAll()
            .map(userMapper::toUserListItem)
            .collectList();
    }
}
