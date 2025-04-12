package com.github.khalaimovda.shopview.showcase.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserRegistrationForm {
    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private Boolean isAdmin = false;
}
