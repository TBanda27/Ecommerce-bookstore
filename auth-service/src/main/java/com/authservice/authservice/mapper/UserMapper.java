package com.authservice.authservice.mapper;

import com.authservice.authservice.dto.UserResponseDTO;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.enums.Role;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class UserMapper {

    public UserResponseDTO mapToResponseDTO(User user) {
        Set<String> roles = user.getRoles()
                                .stream()
                                .map(Role::name)
                                .collect(Collectors.toSet());
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), roles, user.getEnabled());
    }
}
