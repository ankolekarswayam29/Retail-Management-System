package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.*;
import com.retailpro.retailmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Billing operations
 * Contains business logic for billing and invoice management
 */
@Service
@Transactional
public class BillingService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new sale/bill
     */
    public Sale createSale(Sale sale, List<SaleItem> saleItems, Long userId) {
        // Validate sale items
        if (saleItems == null || saleItems.isEmpty()) {
            throw new RuntimeException("Sale must have at least one item");
        }

        // Check stock availability for all items
        for (SaleItem item : saleItems) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStockQuantity() + ", Required: " + item.getQuantity());
            }

            // Set unit price from product
            item.setUnitPrice(product.getSellingPrice());
            item.calculateTotalPrice();
        }

        // Calculate totals
        BigDecimal subtotal = saleItems.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = sale.getTaxAmount() != null ? sale.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = sale.getDiscountAmount() != null ? sale.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount);

        // Set sale details
        if (userId != null && sale.getUser() == null) {
            userRepository.findById(userId).ifPresent(sale::setUser);
        }
        sale.setSubtotal(subtotal);
        sale.setTaxAmount(taxAmount);
        sale.setDiscountAmount(discountAmount);
        sale.setTotalAmount(totalAmount);
        sale.setSaleDate(LocalDateTime.now());
        sale.setInvoiceNumber(generateInvoiceNumber());

        // Save sale
        Sale savedSale = saleRepository.save(sale);

        // Save sale items and update stock
        for (SaleItem item : saleItems) {
            item.setSale(savedSale);
            saleItemRepository.save(item);

            // Update product stock
            Product product = productRepository.findById(item.getProduct().getId()).get();
            int previousQuantity = product.getStockQuantity();
            int newQuantity = previousQuantity - item.getQuantity();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);

            // Record stock transaction
            StockTransaction stockTransaction = new StockTransaction();
            stockTransaction.setProduct(product);
            stockTransaction.setTransactionType(StockTransaction.TYPE_SALE);
            stockTransaction.setQuantity(item.getQuantity());
            stockTransaction.setPreviousQuantity(previousQuantity);
            stockTransaction.setNewQuantity(newQuantity);
            stockTransaction.setReferenceType("SALE");
            stockTransaction.setReferenceId(savedSale.getId());
            stockTransaction.setNotes("Sale: " + savedSale.getInvoiceNumber());
            stockTransactionRepository.save(stockTransaction);
        }

        // Update customer statistics if customer is provided
        if (sale.getCustomer() != null) {
            customerService.updatePurchaseStats(sale.getCustomer().getId(), totalAmount);
        }

        return savedSale;
    }

    /**
     * Get sale by ID
     */
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    /**
     * Get sale by invoice number
     */
    public Sale getSaleByInvoiceNumber(String invoiceNumber) {
        return saleRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    /**
     * Get sale items for a sale
     */
    public List<SaleItem> getSaleItems(Long saleId) {
        return saleItemRepository.findBySaleId(saleId);
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        YearMonth now = YearMonth.now();
        String prefix = "INV-" + now.getYear() + String.format("%02d", now.getMonthValue()) + "-";
        
        // Get count of sales this month
        LocalDateTime startOfMonth = now.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = now.atEndOfMonth().atTime(23, 59, 59);
        long count = saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth).size();
        
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * Calculate bill totals
     */
    public BigDecimal calculateTotal(List<SaleItem> items, BigDecimal tax, BigDecimal discount) {
        BigDecimal subtotal = items.stream()
                .map(SaleItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return subtotal.add(tax != null ? tax : BigDecimal.ZERO)
                .subtract(discount != null ? discount : BigDecimal.ZERO);
    }
}
