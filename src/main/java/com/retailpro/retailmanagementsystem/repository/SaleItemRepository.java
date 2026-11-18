package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SaleItem entity
 * Provides database operations for sale items
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    /**
     * Find sale items by sale
     * @param saleId The sale ID
     * @return List of sale items for the sale
     */
    List<SaleItem> findBySaleId(Long saleId);

    /**
     * Find sale items by product
     * @param productId The product ID
     * @return List of sale items for the product
     */
    List<SaleItem> findByProductId(Long productId);

    /**
     * Find best-selling products
     * @return List of product IDs with their total quantities sold
     */
    @Query("SELECT si.product.id, SUM(si.quantity) as totalSold FROM SaleItem si GROUP BY si.product.id ORDER BY totalSold DESC")
    List<Object[]> findBestSellingProducts();

    /**
     * Calculate total quantity sold for a product
     * @param productId The product ID
     * @return Total quantity sold
     */
    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId")
    Long calculateTotalSoldByProduct(@Param("productId") Long productId);

    /**
     * Top selling products with product name, total quantity, and revenue
     */
    @Query("SELECT si.product.name, SUM(si.quantity), COALESCE(SUM(si.totalPrice), 0) FROM SaleItem si GROUP BY si.product.name ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingProducts();

    /**
     * Category-wise sales summary
     */
    @Query("SELECT si.product.category.name, COALESCE(SUM(si.totalPrice), 0) FROM SaleItem si GROUP BY si.product.category.name ORDER BY SUM(si.totalPrice) DESC")
    List<Object[]> findCategorySalesSummary();

    /**
     * Total Revenue and Total Cost of Goods Sold (Expense)
     */
    @Query("SELECT COALESCE(SUM(si.totalPrice), 0), COALESCE(SUM(si.quantity * si.product.purchasePrice), 0) FROM SaleItem si")
    List<Object[]> calculateRevenueAndExpense();
}
