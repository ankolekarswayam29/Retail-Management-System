package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Category;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Supplier;
import com.retailpro.retailmanagementsystem.repository.CategoryRepository;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;
import com.retailpro.retailmanagementsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Product operations
 * Contains business logic for product management
 */
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get active products only
     */
    public List<Product> getActiveProducts() {
        return productRepository.findAll().stream()
                .filter(Product::getActive)
                .toList();
    }

    /**
     * Get product by ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by product code
     */
    public Optional<Product> getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }

    /**
     * Search products by name or code
     */
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm);
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Get products by supplier
     */
    public List<Product> getProductsBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId);
    }

    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    /**
     * Get out of stock products
     */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    /**
     * Create new product
     * Fetches Category and Supplier from database to prevent TransientPropertyValueException
     */
    public Product createProduct(Product product) {
        // Check if product code already exists
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new RuntimeException("Product code already exists");
        }

        // Fetch Category from database if provided
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Fetch Supplier from database if provided
        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            product.setSupplier(supplier);
        }

        // Set default values
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        if (product.getMinimumStock() == null) {
            product.setMinimumStock(10);
        }
        if (product.getUnit() == null) {
            product.setUnit("pcs");
        }
        if (product.getActive() == null) {
            product.setActive(true);
        }

        return productRepository.save(product);
    }

    /**
     * Update product
     * Fetches Category and Supplier from database to prevent TransientPropertyValueException
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update fields
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());

        // Fetch and update Category if provided
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Fetch and update Supplier if provided
        if (productDetails.getSupplier() != null && productDetails.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(productDetails.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            product.setSupplier(supplier);
        }

        product.setPurchasePrice(productDetails.getPurchasePrice());
        product.setSellingPrice(productDetails.getSellingPrice());
        product.setMinimumStock(productDetails.getMinimumStock());
        product.setUnit(productDetails.getUnit());
        product.setImagePath(productDetails.getImagePath());
        product.setActive(productDetails.getActive());

        // Update stock quantity if provided
        if (productDetails.getStockQuantity() != null) {
            product.setStockQuantity(productDetails.getStockQuantity());
        }

        return productRepository.save(product);
    }

    /**
     * Delete product (soft delete by setting active to false)
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    /**
     * Permanently delete product
     */
    public void permanentlyDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    /**
     * Update product stock
     */
    public Product updateStock(Long productId, Integer quantityChange, String transactionType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int newQuantity = product.getStockQuantity() + quantityChange;

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }

    /**
     * Get total product count
     */
    public long getTotalProductCount() {
        return productRepository.count();
    }

    /**
     * Get total active product count
     */
    public long getActiveProductCount() {
        return productRepository.findAll().stream()
                .filter(Product::getActive)
                .count();
    }
}
