package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Sale entity
 * Provides database operations for sales
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Find sale by invoice number
     * @param invoiceNumber The invoice number
     * @return Optional containing the sale if found
     */
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find sales by customer
     * @param customerId The customer ID
     * @return List of sales for the customer
     */
    List<Sale> findByCustomerId(Long customerId);

    /**
     * Find sales by user
     * @param userId The user ID
     * @return List of sales by the user
     */
    List<Sale> findByUserId(Long userId);

    /**
     * Find sales between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return List of sales in the date range
     */
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find today's sales
     * @return List of today's sales
     */
    @Query("SELECT s FROM Sale s WHERE DATE(s.saleDate) = CURRENT_DATE")
    List<Sale> findTodaySales();

    /**
     * Find sales for a specific month
     * @param year The year
     * @param month The month (1-12)
     * @return List of sales for the month
     */
    @Query("SELECT s FROM Sale s WHERE YEAR(s.saleDate) = :year AND MONTH(s.saleDate) = :month")
    List<Sale> findSalesByMonth(@Param("year") int year, @Param("month") int month);

    /**
     * Calculate total revenue for a date range
     * @param startDate The start date
     * @param endDate The end date
     * @return Total revenue
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate today's revenue
     * @return Today's total revenue
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE DATE(s.saleDate) = CURRENT_DATE")
    Double calculateTodayRevenue();

    /**
     * Calculate all-time revenue
     * @return All-time total revenue
     */
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s")
    Double calculateAllTimeRevenue();

    /**
     * Get recent sales (last N sales)
     * @param limit The number of recent sales to retrieve
     * @return List of recent sales
     */
    @Query("SELECT s FROM Sale s ORDER BY s.saleDate DESC")
    List<Sale> findRecentSales();
}
