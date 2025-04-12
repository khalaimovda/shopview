package com.github.khalaimovda.shopview.showcase.mapper;

import com.github.khalaimovda.shopview.showcase.dto.UserRegistrationForm;
import com.github.khalaimovda.shopview.showcase.model.User;
import com.github.khalaimovda.shopview.showcase.model.UserRole;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class UserMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword"),
        @Mapping(source = "isAdmin", target = "roles", qualifiedByName = "setUserRoles")
    })
    public abstract User toUser(UserRegistrationForm registrationForm, @Context PasswordEncoder passwordEncoder);

    @Named("setUserRoles")
    public List<UserRole> setUserRoles(Boolean isAdmin) {
        return isAdmin != null && isAdmin ? List.of(UserRole.ADMIN) : List.of();
    }

    @Named("encodePassword")
    public String encodePassword(String password, @Context PasswordEncoder encoder) {
        return encoder.encode(password);
    }
}