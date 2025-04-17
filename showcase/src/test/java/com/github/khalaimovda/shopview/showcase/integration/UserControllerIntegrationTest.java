package com.github.khalaimovda.shopview.showcase.integration;

import com.github.khalaimovda.shopview.showcase.dto.UserListItem;
import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private UserService userService;

    @Test
    void testShowRegistrationForm() {
        Authentication auth = createAuthentication(adminUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/users/registration")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("Зарегистрировать пользователя"));
                assertTrue(body.contains("form-login"));
                assertTrue(body.contains("form-password"));
                assertTrue(body.contains("form-admin"));
            });
    }

    @Test
    void testShowRegistrationFormAnonymousRedirected() {
        webTestClient.get()
            .uri("/users/registration")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testCreateUser() {
        Authentication auth = createAuthentication(adminUser);

        UserRegistrationForm form = new UserRegistrationForm();
        form.setUsername("newuser");
        form.setPassword("password123");
        form.setIsAdmin(false);

        when(userService.createUser(any(UserRegistrationForm.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("username", form.getUsername())
                .with("password", form.getPassword())
                .with("username", form.getUsername())
                .with("isAdmin", "true"))
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/users");

        verify(userService, times(1)).createUser(any(UserRegistrationForm.class));
    }

    @Test
    void testCreateUserWithoutCsrfForbidden() {
        Authentication auth = createAuthentication(adminUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("username", "test"))
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void testCreateUserAnonymousRedirected() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("username", "test"))
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    void testCreateUserForbiddenForNonAdmin() {
        Authentication auth = createAuthentication(ordinaryUser);

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/users")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("username", "test"))
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    void testGetUsers() {
        Authentication auth = createAuthentication(adminUser);

        UserListItem user1 = new UserListItem();
        user1.setId(1L);
        user1.setUsername("admin");
        user1.setIsAdmin(true);

        UserListItem user2 = new UserListItem();
        user2.setId(2L);
        user2.setUsername("user");
        user2.setIsAdmin(false);

        when(userService.getAllUsers()).thenReturn(Mono.just(List.of(user1, user2)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
            .get()
            .uri("/users")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(response -> {
                String body = response.getResponseBody();
                assertNotNull(body);
                assertTrue(body.contains("Пользователи"));
                assertTrue(body.contains("admin"));
                assertTrue(body.contains("user"));
                assertTrue(body.contains("ADMIN"));
            });
    }

    @Test
    void testGetUsersAnonymousRedirected() {
        webTestClient.get()
            .uri("/users")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/login");
    }
}
