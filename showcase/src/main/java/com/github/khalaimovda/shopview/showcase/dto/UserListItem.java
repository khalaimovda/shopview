package com.github.khalaimovda.shopview.showcase.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserListItem {
    @NotNull
    @Min(1L)
    private Long id;

    @NotBlank
    private String username;

    private Boolean isAdmin = false;
}
