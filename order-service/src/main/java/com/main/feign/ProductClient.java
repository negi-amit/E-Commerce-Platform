package com.main.feign;

import com.main.dtos.ProductResponse;
import com.main.dtos.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ResponseEntity<ProductResponse> getProductDetails(@PathVariable String id);

    @GetMapping("/products/batch")
    ResponseEntity<List<ProductResponse>> getProductsByIds(@RequestParam List<String> productIds);

    @PutMapping("/products/update-stock")
    ResponseEntity<String> updateStock(@RequestBody StockUpdateRequest request);

    @PutMapping("/products/{id}/update-stock")
    ResponseEntity<String> updateStock(@PathVariable("id") String productId,
                                       @RequestParam("quantityChange") int quantityChange);
}
