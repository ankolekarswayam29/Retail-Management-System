package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Product;

import com.retailpro.retailmanagementsystem.entity.StockTransaction;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;
import com.retailpro.retailmanagementsystem.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Inventory operations
 * Contains business logic for inventory and stock management
 */
@Service
@Transactional
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    /**
     * Get all products with stock information
     */
    public List<Product> getAllInventory() {
        return productRepository.findAll();
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
     * Add stock to product
     */
    public Product addStock(Long productId, Integer quantity, String notes, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity + quantity;
        product.setStockQuantity(newQuantity);

        // Record stock transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(StockTransaction.TYPE_PURCHASE);
        transaction.setQuantity(quantity);
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReferenceType("MANUAL");
        transaction.setReferenceId(null);
        transaction.setNotes(notes);
        
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            transaction.setCreatedBy(user);
        }

        stockTransactionRepository.save(transaction);
        return productRepository.save(product);
    }

    /**
     * Reduce stock from product
     */
    public Product reduceStock(Long productId, Integer quantity, String notes, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be positive");
        }

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity - quantity;
        product.setStockQuantity(newQuantity);

        // Record stock transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(StockTransaction.TYPE_ADJUSTMENT);
        transaction.setQuantity(quantity);
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReferenceType("MANUAL");
        transaction.setReferenceId(null);
        transaction.setNotes(notes);
        
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            transaction.setCreatedBy(user);
        }

        stockTransactionRepository.save(transaction);
        return productRepository.save(product);
    }

    /**
     * Adjust stock to specific quantity
     */
    public Product adjustStock(Long productId, Integer newQuantity, String notes, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (newQuantity < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        }

        int previousQuantity = product.getStockQuantity();
        int quantityChange = newQuantity - previousQuantity;
        product.setStockQuantity(newQuantity);

        // Record stock transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(StockTransaction.TYPE_ADJUSTMENT);
        transaction.setQuantity(quantityChange);
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReferenceType("MANUAL");
        transaction.setReferenceId(null);
        transaction.setNotes(notes);
        
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            transaction.setCreatedBy(user);
        }

        stockTransactionRepository.save(transaction);
        return productRepository.save(product);
    }

    /**
     * Get stock transaction history for a product
     */
    public List<StockTransaction> getProductStockHistory(Long productId) {
        return stockTransactionRepository.findByProductId(productId);
    }

    /**
     * Get recent stock transactions
     */
    public List<StockTransaction> getRecentStockTransactions() {
        List<StockTransaction> transactions = stockTransactionRepository.findRecentTransactions();
        return transactions.size() > 20 ? transactions.subList(0, 20) : transactions;
    }

    /**
     * Get stock transactions by type
     */
    public List<StockTransaction> getStockTransactionsByType(String transactionType) {
        return stockTransactionRepository.findByTransactionType(transactionType);
    }

    /**
     * Get stock transactions between dates
     */
    public List<StockTransaction> getStockTransactionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return stockTransactionRepository.findByCreatedAtBetween(startDate, endDate);
    }
}
