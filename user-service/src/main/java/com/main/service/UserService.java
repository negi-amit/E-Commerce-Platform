package com.main.service;

import com.main.dtos.UserRequest;
import com.main.dtos.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse registerUser(UserRequest userRequest);

    UserResponse getUserDetails(String id);

    List<UserResponse> getAllUsersDetails();

    UserResponse updateUserDetails(String id, UserRequest userRequest);

    String deleteUser(String id);
}
