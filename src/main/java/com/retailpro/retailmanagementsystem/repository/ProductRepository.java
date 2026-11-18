package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * Provides database operations for products
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by product code
     * @param productCode The product code
     * @return Optional containing the product if found
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Find products by name containing the search term
     * @param name The search term
     * @return List of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by category
     * @param categoryId The category ID
     * @return List of products in the category
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products by category name
     * @param categoryName The category name
     * @return List of products in the category
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    /**
     * Find products by supplier
     * @param supplierId The supplier ID
     * @return List of products from the supplier
     */
    List<Product> findBySupplierId(Long supplierId);

    /**
     * Find products with low stock (stock quantity <= minimum stock)
     * @return List of low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStock AND p.active = true")
    List<Product> findLowStockProducts();

    /**
     * Find products that are out of stock
     * @return List of out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.active = true")
    List<Product> findOutOfStockProducts();

    /**
     * Search products by name or code
     * @param searchTerm The search term
     * @return List of matching products
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.productCode LIKE %:searchTerm%")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * Check if product code exists
     * @param productCode The product code to check
     * @return true if product code exists, false otherwise
     */
    boolean existsByProductCode(String productCode);
}
