package com.authservice.authservice.controller;

import com.authservice.authservice.dto.UserRegistrationRequestDTO;
import com.authservice.authservice.dto.UserResponseDTO;
import com.authservice.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@Tag(name = "User Management", description = "User CRUD operations with role-based access control")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(
        summary = "Register New User",
        description = "Public endpoint to register a new user. No authentication required.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
        }
    )
    @SecurityRequirements() // Public endpoint
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRegistrationRequestDTO userRegistrationRequestDTO) {
        log.info("userController: registerUser called with username: {}", userRegistrationRequestDTO);
        return new ResponseEntity<>(userService.registerUser(userRegistrationRequestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get All Users (Admin Only)",
        description = "Retrieve paginated list of all users. Requires ROLE_ADMIN.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("Book Controller: Request to get all books - page: {}, size: {}", page, size);
        return new ResponseEntity<>(userService.getAllUsers(page, size), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(
        summary = "Get User By ID",
        description = "Retrieve user details. Admins can view any user, regular users can only view their own profile.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("User Controller: Request to get user by id: {}", id);
        return new ResponseEntity<>(userService.findUserById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(
        summary = "Update User",
        description = "Update user details. Admins can update any user, regular users can only update their own profile.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody UserRegistrationRequestDTO userUpdateRequestDTO) {
        log.info("User Controller: Request to update user with id: {}", id);
        return new ResponseEntity<>(userService.updateUser(id, userUpdateRequestDTO), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(
        summary = "Delete User",
        description = "Delete a user. Admins can delete any user, regular users can delete their own account.",
        responses = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("User Controller: Request to delete user with id: {}", id);
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/internal/{id}")
    @Operation(
        summary = "Get User By ID (Internal)",
        description = "Internal endpoint for service-to-service communication. No authentication required.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @SecurityRequirements() // No authentication required for internal calls
    public ResponseEntity<UserResponseDTO> getUserByIdInternal(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("User Controller (Internal): Request to get user by id: {}", id);
        return new ResponseEntity<>(userService.findUserById(id), HttpStatus.OK);
    }
}
