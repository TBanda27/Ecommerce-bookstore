package com.authservice.authservice.service;

import com.authservice.authservice.dto.UserRegistrationRequestDTO;
import com.authservice.authservice.dto.UserResponseDTO;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.enums.Role;
import com.authservice.authservice.mapper.UserMapper;
import com.authservice.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
        log.info("User Service: UserRegistrationRequestDTO:{}", userRegistrationRequestDTO);
        userRepository.findByEmail(userRegistrationRequestDTO.email())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("User Registration Failed: Email already in use");
                });
        checkPasswordMatch(userRegistrationRequestDTO);
        User user = User.builder()
                .username(userRegistrationRequestDTO.username())
                .email(userRegistrationRequestDTO.email())
                .password(bCryptPasswordEncoder.encode(userRegistrationRequestDTO.password()))
                .roles(Set.of(Role.ROLE_USER))
                .build();
        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.mapToResponseDTO(savedUser);
    }

    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        log.info("User Service: Get All Users: Page: {}, Size: {}", page, size);
        Page<User> usersPage = userRepository.findAll(PageRequest.of(page, size));
        return usersPage.map(userMapper::mapToResponseDTO);
    }

    public UserResponseDTO findUserById(Long id) {
        log.info("User Service: Find User by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return userMapper.mapToResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRegistrationRequestDTO userUpdateRequestDTO) {
        log.info("User Service: Update User with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setUsername(userUpdateRequestDTO.username());
        user.setEmail(userUpdateRequestDTO.email());
        checkPasswordMatch(userUpdateRequestDTO);
        user.setPassword(bCryptPasswordEncoder.encode(userUpdateRequestDTO.password()));
        User updatedUser = userRepository.saveAndFlush(user);
        return userMapper.mapToResponseDTO(updatedUser);
    }
    public void checkPasswordMatch(UserRegistrationRequestDTO userDTO) {
        if(userDTO.password() == null || userDTO.confirmPassword() == null) {
            throw new IllegalArgumentException("Password and Confirm Password cannot be null");
        }
        if (!Objects.equals(userDTO.password(), userDTO.confirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password do not match");
        }
    }

    public void deleteUser(Long id) {
        log.info("User Service: Delete User with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("UserService:loadUserByUsername {}", username);
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
