package com.retailpro.retailmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * StockTransaction Entity
 * Represents stock movement history (purchases, sales, returns, adjustments)
 */
@Entity
@Table(name = "stock_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Product
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // PURCHASE, SALE, RETURN, ADJUSTMENT, DAMAGE

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // SALE, PURCHASE_ORDER, MANUAL

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Many-to-One relationship with User (who created the transaction)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Transaction type constants
    public static final String TYPE_PURCHASE = "PURCHASE";
    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_RETURN = "RETURN";
    public static final String TYPE_ADJUSTMENT = "ADJUSTMENT";
    public static final String TYPE_DAMAGE = "DAMAGE";
}
