package com.github.khalaimovda.shopview.showcase.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;


@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private List<UserRole> roles = new ArrayList<>();
}
