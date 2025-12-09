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

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    public UserResponseDTO registerUser(UserRegistrationRequestDTO userRegistrationRequestDTO) {
        log.info("User Service: UserRegistrationRequestDTO:{}", userRegistrationRequestDTO);
        userRepository.findByEmail(userRegistrationRequestDTO.email())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("User Registration Failed: Email already in use");
                });
        String verificationToken = java.util.UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);
        checkPasswordMatch(userRegistrationRequestDTO);
        User user = User.builder()
                .username(userRegistrationRequestDTO.username())
                .email(userRegistrationRequestDTO.email())
                .password(bCryptPasswordEncoder.encode(userRegistrationRequestDTO.password()))
                .roles(Set.of(Role.ROLE_USER))
                .verificationToken(verificationToken)
                .tokenExpirationTime(expiration)
                .enabled(false)
                .build();
        User savedUser = userRepository.saveAndFlush(user);

        // Send verification email
        emailService.sendVerificationToken(savedUser.getEmail(), savedUser.getUsername(), verificationToken);
        log.info("Verification email sent to: {}", savedUser.getEmail());

        return userMapper.mapToResponseDTO(savedUser);
    }

    public String verifyUser(String verificationToken) {
        log.info("User Service: VerificationToken:{}", verificationToken);
        User user = userRepository.findByVerificationToken(verificationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));
        if (user.getTokenExpirationTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        } else {
            user.setEnabled(true);
            user.setVerificationToken(null);
            user.setTokenExpirationTime(null);
            userRepository.saveAndFlush(user);
            return user.getUsername() + " has been successfully verified. You can now log in.";
        }
    }

    public void resendVerificationEmail(String email){
        log.info("User Service: Resend Verification Email to: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        if(user.getEnabled()){
            throw new IllegalArgumentException("User is already verified.");
        }
        String newVerificationToken = java.util.UUID.randomUUID().toString();
        LocalDateTime newExpiration = LocalDateTime.now().plusHours(1);
        user.setVerificationToken(newVerificationToken);
        user.setTokenExpirationTime(newExpiration);
        userRepository.saveAndFlush(user);

        emailService.sendVerificationToken(user.getEmail(), user.getUsername(), newVerificationToken);
        log.info("Verification email resent to: {}", email);
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
