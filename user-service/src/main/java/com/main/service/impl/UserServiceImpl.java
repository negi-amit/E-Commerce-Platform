package com.main.service.impl;

import com.main.dtos.UserRequest;
import com.main.dtos.UserResponse;
import com.main.entity.User;
import com.main.repository.UserRepository;
import com.main.service.UserService;
import com.main.util.UserServiceContant;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserResponse registerUser(UserRequest userRequest) {
        log.info("Registering new user with email: {}", userRequest.getEmail());
        User user = modelMapper.map(userRequest, User.class);
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse getUserDetails(String id) {
        log.info("Fetching details for user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(UserServiceContant.USER_NOT_FOUND + id));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.warn("Attempted to access deleted user with ID: {}", id);
            throw new IllegalStateException("User has been deleted");
        }

        log.info("User details retrieved successfully for ID: {}", id);
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUsersDetails() {
        log.info("Fetching all active users");
        List<User> users = userRepository.findAllByIsDeletedFalse();
        log.info("Found {} active users", users.size());
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class)).toList();
    }

    @Override
    @Transactional
    public UserResponse updateUserDetails(String id, UserRequest userRequest) {
        log.info("Updating user details for ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() ->  new RuntimeException(UserServiceContant.USER_NOT_FOUND + id));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.warn("Attempted to update a deleted user with ID: {}", id);
            throw new IllegalStateException("Cannot update a deleted user");
        }

        modelMapper.map(userRequest, user);
        User updatedUser = userRepository.save(user);
        log.info("User details updated successfully for ID: {}", id);
        return modelMapper.map(updatedUser, UserResponse.class);
    }

    @Override
    @Transactional
    public String deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException(UserServiceContant.USER_NOT_FOUND + id);
                });

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.warn("User with ID: {} is already marked as deleted", id);
            throw new IllegalStateException("User is already deleted");
        }

        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("User successfully marked as deleted with ID: {}", id);
        return UserServiceContant.USER_DELETED_RESPONSE;
    }
}
