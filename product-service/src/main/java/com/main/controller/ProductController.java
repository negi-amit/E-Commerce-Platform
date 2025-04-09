package com.main.controller;

import com.main.dtos.ProductRequest;
import com.main.dtos.ProductResponse;
import com.main.dtos.StockUpdateRequest;
import com.main.repository.ProductRepository;
import com.main.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
//@RequiredArgsConstructor
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService, ModelMapper modelMapper) {
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Add a new product", description = "Creates a new product in the system")
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping("/add")
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        log.info("Received request to add a product");
        ProductResponse response = productService.addProduct(productRequest);
        log.info("Product added successfully with ID");
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Get product details", description = "Retrieves product details by ID")
    @ApiResponse(responseCode = "200", description = "Product details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public ResponseEntity<Optional<ProductResponse>> getProductDetails(@PathVariable("id") String id) {
        log.info("Fetching product details for ID: {}", id);
        Optional<ProductResponse> response = productService.getProductDetails(id);
        log.info("Product details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all products", description = "Fetches details of all products")
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Fetching all product details...");
        List<ProductResponse> responseList = productService.getAllProductsDetails();
        log.info("Total products retrieved: {}", responseList.size());
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Update product details", description = "Updates product information by ID")
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") String id,
            @Valid @RequestBody ProductRequest productRequest) {
        log.info("Updating product details for ID: {}", id);
        ProductResponse response = productService.updateProduct(id, productRequest);
        log.info("Product details updated successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete product", description = "Deletes a product by ID")
    @ApiResponse(responseCode = "200", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") String id) {
        log.info("Deleting product with ID: {}", id);
        String message = productService.deleteProduct(id);
        log.info("Product deleted successfully with ID: {}", id);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@RequestParam List<String> productIds) {
        List<ProductResponse> products = productService.getProductsByIds(productIds);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/update-stock")
    public ResponseEntity<String> updateStock(@RequestBody StockUpdateRequest request) {
        productService.updateStock(request);
        return ResponseEntity.ok("Stock updated successfully");
    }
}
