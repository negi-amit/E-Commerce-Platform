package com.main.repository;

import com.main.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    void findByUserId(String userId);

    List<Order> findAllByUserId(String userId);
}
