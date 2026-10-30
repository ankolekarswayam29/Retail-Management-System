package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for StockTransaction entity
 * Provides database operations for stock transactions
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    /**
     * Find stock transactions by product
     * @param productId The product ID
     * @return List of stock transactions for the product
     */
    List<StockTransaction> findByProductId(Long productId);

    /**
     * Find stock transactions by transaction type
     * @param transactionType The transaction type
     * @return List of stock transactions of the type
     */
    List<StockTransaction> findByTransactionType(String transactionType);

    /**
     * Find stock transactions between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return List of stock transactions in the date range
     */
    List<StockTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find stock transactions by reference
     * @param referenceType The reference type
     * @param referenceId The reference ID
     * @return List of stock transactions for the reference
     */
    List<StockTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    /**
     * Find recent stock transactions
     * @param limit The number of recent transactions
     * @return List of recent stock transactions
     */
    @Query("SELECT st FROM StockTransaction st ORDER BY st.createdAt DESC")
    List<StockTransaction> findRecentTransactions();

    /**
     * Find stock transactions created by a specific user
     * @param userId The user ID
     * @return List of stock transactions created by the user
     */
    List<StockTransaction> findByCreatedById(Long userId);
}
