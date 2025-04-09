package com.main.feign_client;

import com.main.dtos.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/order/{id}")
    public OrderResponse getOrderDetails(@PathVariable String id);

    @PutMapping("/order/{id}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable String id, @RequestParam String status);

}
