
package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.dto.ProductMapper;
import com.retailpro.retailmanagementsystem.dto.ProductRequest;
import com.retailpro.retailmanagementsystem.dto.ProductResponse;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts()
                .stream()
                .map(product -> ProductMapper.toResponse(product))
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ProductMapper.toResponse(product))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductRequest productRequest) {

        Product product = ProductMapper.toEntity(productRequest);
        Product savedProduct = productService.createProduct(product);
        ProductResponse response = ProductMapper.toResponse(savedProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest productRequest) {

        Product productDetails = ProductMapper.toEntity(productRequest);
        Product updatedProduct = productService.updateProduct(id, productDetails);
        ProductResponse response = ProductMapper.toResponse(updatedProduct);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String keyword) {

        List<ProductResponse> products = productService.searchProducts(keyword)
                .stream()
                .map(product -> ProductMapper.toResponse(product))
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        List<ProductResponse> products = productService.getLowStockProducts()
                .stream()
                .map(product -> ProductMapper.toResponse(product))
                .toList();

        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductResponse>> getOutOfStockProducts() {
        List<ProductResponse> products = productService.getOutOfStockProducts()
                .stream()
                .map(product -> ProductMapper.toResponse(product))
                .toList();

        return ResponseEntity.ok(products);
    }
}
