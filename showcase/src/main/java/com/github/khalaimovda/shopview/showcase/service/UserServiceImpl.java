package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.mapper.UserMapper;
import com.github.khalaimovda.shopview.showcase.model.User;
import com.github.khalaimovda.shopview.showcase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> createUser(UserRegistrationForm form) {
        User user = userMapper.toUser(form, passwordEncoder);
        return userRepository.save(user);
    }
}
