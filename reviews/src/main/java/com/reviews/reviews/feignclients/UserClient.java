package com.reviews.reviews.feignclients;

import com.reviews.reviews.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", path = "/api/v1/user/internal")
public interface UserClient {

    @GetMapping("/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable("id") Long id);

    @GetMapping("/username/{username}")
    ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable("username") String username);
}