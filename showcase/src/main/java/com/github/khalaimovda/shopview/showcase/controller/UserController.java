package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/registration")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> showRegistrationForm() {
        return Mono.just("registration");
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createUser(@Valid @ModelAttribute UserRegistrationForm form) {
        return userService
            .createUser(form)
            .then(Mono.just("redirect:/users"));
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> getUsers(Model model) {
        return userService.
            getAllUsers()
            .map(users -> model.addAttribute("users", users))
            .thenReturn("users");
    }
}
