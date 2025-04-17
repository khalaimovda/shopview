package com.github.khalaimovda.shopview.showcase.controller;


import com.github.khalaimovda.shopview.showcase.dto.UserListItem;
import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    @Captor
    private ArgumentCaptor<UserRegistrationForm> userRegistrationFormCaptor;


    @Test
    void testShowRegistrationForm() {
        StepVerifier
            .create(userController.showRegistrationForm())
            .expectNext("registration")
            .verifyComplete();
    }

    @Test
    void testCreateUser() {
        UserRegistrationForm form = new UserRegistrationForm();
        form.setUsername("test-username");
        form.setPassword("test-password");
        form.setIsAdmin(false);
        when(userService.createUser(any(UserRegistrationForm.class))).thenReturn(Mono.empty());

        StepVerifier
            .create(userController.createUser(form))
            .expectNext("redirect:/users")
            .verifyComplete();

        verify(userService, times(1)).createUser(userRegistrationFormCaptor.capture());
        assertEquals(form, userRegistrationFormCaptor.getValue());
    }

    @Test
    void testGetUsers() {
        UserListItem user = new UserListItem();
        user.setId(22L);
        user.setUsername("database-user");
        user.setIsAdmin(true);
        List<UserListItem> users = List.of(user);
        when(userService.getAllUsers()).thenReturn(Mono.just(users));

        StepVerifier
            .create(userController.getUsers(model))
            .expectNext("users")
            .verifyComplete();

        verify(userService, times(1)).getAllUsers();
        verify(model).addAttribute("users", users);
    }
}
