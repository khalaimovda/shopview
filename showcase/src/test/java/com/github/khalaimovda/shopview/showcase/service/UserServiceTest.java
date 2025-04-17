package com.github.khalaimovda.shopview.showcase.service;

import com.github.khalaimovda.shopview.showcase.dto.UserListItem;
import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.mapper.UserMapper;
import com.github.khalaimovda.shopview.showcase.model.User;
import com.github.khalaimovda.shopview.showcase.model.UserRole;
import com.github.khalaimovda.shopview.showcase.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private UserServiceImpl userService;

    private final BigDecimal defaultBalance = new BigDecimal("123.45");

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "defaultBalance", defaultBalance);
    }


    @Test
    void testCreateUser() {
        UserRegistrationForm form = new UserRegistrationForm();
        form.setUsername("test-username");
        form.setPassword("test-password");
        form.setIsAdmin(true);

        User createdUser = new User();
        createdUser.setId(13L);
        createdUser.setUsername("test-username");
        createdUser.setPassword("test-password-encoded");
        createdUser.setRoles(List.of(UserRole.ADMIN));

        when(passwordEncoder.encode(anyString())).thenReturn("test-password-encoded");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(createdUser));
        when(paymentService.addBalance(anyLong(), any(BigDecimal.class))).thenReturn(Mono.empty());

        StepVerifier
            .create(userService.createUser(form))
            .assertNext(user -> assertEquals(createdUser, user))
            .verifyComplete();

        verify(userRepository, times(1)).save(any(User.class));
        verify(paymentService, times(1)).addBalance(eq(createdUser.getId()), eq(defaultBalance));

    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setId(13L);
        user.setUsername("test-username");
        user.setPassword("test-password-encoded");
        user.setRoles(List.of(UserRole.ADMIN));

        UserListItem userListItem = new UserListItem();
        userListItem.setId(user.getId());
        userListItem.setUsername(user.getUsername());
        userListItem.setIsAdmin(true);
        List<UserListItem> userListItems = List.of(userListItem);

        when(userRepository.findAll()).thenReturn(Flux.just(user));

        StepVerifier
            .create(userService.getAllUsers())
            .assertNext(result -> assertEquals(userListItems, result))
            .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }
}
