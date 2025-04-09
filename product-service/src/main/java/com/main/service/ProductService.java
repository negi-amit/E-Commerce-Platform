package com.main.service;

import com.main.dtos.ProductRequest;
import com.main.dtos.ProductResponse;
import com.main.dtos.StockUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductResponse addProduct(ProductRequest productRequest);

    Optional<ProductResponse> getProductDetails(String id);

    List<ProductResponse> getAllProductsDetails();

    ProductResponse updateProduct(String id, ProductRequest productRequest);

    String deleteProduct(String id);

    List<ProductResponse> getProductsByIds(List<String> productIds);

    void updateStock(StockUpdateRequest request);
}
