package com.main.service.impl;

import com.main.dtos.ProductRequest;
import com.main.dtos.ProductResponse;
import com.main.dtos.StockUpdateRequest;
import com.main.entity.Product;
import com.main.exceptions.BusinessException;
import com.main.exceptions.ProductException;
import com.main.repository.ProductRepository;
import com.main.service.ProductService;
import com.main.util.ProductServiceContant;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductResponse addProduct(ProductRequest productRequest) {
        log.info("Registering new user with email: ");
        Product product = modelMapper.map(productRequest, Product.class);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(null);
        Product savedProduct = productRepository.save(product);
        log.info("Product registered successfully with ID: {}", savedProduct.getId());
        return modelMapper.map(savedProduct, ProductResponse.class);
    }

    @Override
    public Optional<ProductResponse> getProductDetails(String id)  {
        log.info("Fetching details for product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductServiceContant.PRODUCT_NOT_FOUND + id, HttpStatus.BAD_REQUEST));
        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            log.warn("Attempted to access deleted product with ID: {}", id);
            throw new IllegalStateException("Product has been deleted");
        }

        log.info("Product details retrieved successfully for ID: {}", id);
        return Optional.ofNullable(modelMapper.map(product, ProductResponse.class));
    }

    @Override
    public List<ProductResponse> getAllProductsDetails() {
        log.info("Fetching all products");
        List<Product> products = productRepository.findAllByIsDeletedFalse();
        log.info("Found {} active users", products.size());
        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponse.class)).toList();
    }

    @Override
    public ProductResponse updateProduct(String id, ProductRequest productRequest) {
        log.info("Updating product details for ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() ->  new BusinessException(ProductServiceContant.PRODUCT_NOT_FOUND + id, HttpStatus.BAD_REQUEST));

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            log.warn("Attempted to update a deleted product with ID: {}", id);
            throw new IllegalStateException("Cannot update a deleted product");
        }

        modelMapper.map(productRequest, product);
        product.setUpdatedAt(LocalDateTime.now());
        Product updatedUser = productRepository.save(product);
        log.info("Product details updated successfully for ID: {}", id);
        return modelMapper.map(updatedUser, ProductResponse.class);
    }

    @Override
    public String deleteProduct(String id) {
        log.info("Deleting user with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new BusinessException(ProductServiceContant.PRODUCT_NOT_FOUND + id, HttpStatus.BAD_REQUEST);
                });

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            log.warn("Product with ID: {} is already marked as deleted", id);
            throw new IllegalStateException("Product is already deleted");
        }

        product.setIsDeleted(true);
        productRepository.save(product);
        log.info("Product successfully marked as deleted with ID: {}", id);
        return ProductServiceContant.PRODUCT_DELETED_RESPONSE;
    }

    @Override
    public List<ProductResponse> getProductsByIds(List<String> productIds) {
        List<Product> productList = productRepository.findAllById(productIds);
        List<ProductResponse> productResponseList = new ArrayList<>();
        for (Product product : productList) {
           productResponseList.add(modelMapper.map(product, ProductResponse.class));
        }
        return productResponseList;
    }

    @Override
    public void updateStock(StockUpdateRequest request) {
        List<String> productIds = request.getStockUpdates().stream()
                .map(StockUpdateRequest.StockUpdateItem::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new ProductException("No products found for the given IDs");
        }

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (StockUpdateRequest.StockUpdateItem item : request.getStockUpdates()) {
            Product product = productMap.get(item.getProductId());

            if (product == null) {
                throw new ProductException("Product not found: " + item.getProductId());
            }

            if (product.getStockQuantity() + item.getQuantityChange() < 0) {
                throw new ProductException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() + item.getQuantityChange());
        }

        productRepository.saveAll(products);
    }
}
