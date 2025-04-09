package com.main.feign;

import com.main.dtos.UserResponse;
import org.springframework.stereotype.Component;

@Component
class UserClientFallback implements UserClient {
    @Override
    public UserResponse getUserById(String id) {
        return new UserResponse("N/A", "Default User", "default@example.com","N/A");
    }
}