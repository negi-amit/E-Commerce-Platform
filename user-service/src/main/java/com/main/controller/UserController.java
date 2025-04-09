package com.main.controller;

import com.main.dtos.UserRequest;
import com.main.dtos.UserResponse;
import com.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Operation(summary = "Register a new user", description = "Creates a new user in the system")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Received request to register user: {}", userRequest.getEmail());
        UserResponse response = userService.registerUser(userRequest);
        log.info("User registered successfully with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user details", description = "Retrieves user details by ID")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserDetails(@PathVariable("id") String id) {
        log.info("Fetching user details for ID: {}", id);
        UserResponse response = userService.getUserDetails(id);
        log.info("User details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Fetches details of all users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping("/")
    public ResponseEntity<List<UserResponse>> getAllUsersDetails() {
        log.info("Fetching all user details...");
        List<UserResponse> responseList = userService.getAllUsersDetails();
        log.info("Total users retrieved: {}", responseList.size());
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Update user details", description = "Updates user information by ID")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserDetails(
            @PathVariable("id") String id,
            @Valid @RequestBody UserRequest userRequest) {
        log.info("Updating user details for ID: {}", id);
        UserResponse response = userService.updateUserDetails(id, userRequest);
        log.info("User details updated successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by ID")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") String id) {
        log.info("Deleting user with ID: {}", id);
        String message = userService.deleteUser(id);
        log.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.ok(message);
    }
}
