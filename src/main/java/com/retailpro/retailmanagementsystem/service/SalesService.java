package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Sales operations
 * Contains business logic for sales management and reporting
 */
@Service
@Transactional
public class SalesService {

    @Autowired
    private SaleRepository saleRepository;

    /**
     * Get all sales
     */
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    /**
     * Get sale by ID
     */
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    /**
     * Get sales by customer
     */
    public List<Sale> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId);
    }

    /**
     * Get sales by user
     */
    public List<Sale> getSalesByUser(Long userId) {
        return saleRepository.findByUserId(userId);
    }

    /**
     * Get sales between two dates
     */
    public List<Sale> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate);
    }

    /**
     * Get today's sales
     */
    public List<Sale> getTodaySales() {
        return saleRepository.findTodaySales();
    }

    /**
     * Get sales for a specific month
     */
    public List<Sale> getSalesByMonth(int year, int month) {
        return saleRepository.findSalesByMonth(year, month);
    }

    /**
     * Get recent sales
     */
    public List<Sale> getRecentSales() {
        List<Sale> sales = saleRepository.findRecentSales();
        return sales.size() > 10 ? sales.subList(0, 10) : sales;
    }

    /**
     * Calculate total revenue for a date range
     */
    public Double calculateTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.calculateTotalRevenue(startDate, endDate);
    }

    /**
     * Calculate today's revenue
     */
    public Double calculateTodayRevenue() {
        return saleRepository.calculateTodayRevenue();
    }

    /**
     * Calculate monthly revenue
     */
    public Double calculateMonthlyRevenue(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = LocalDateTime.of(year, month, 
                java.time.YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59);
        return saleRepository.calculateTotalRevenue(startOfMonth, endOfMonth);
    }

    /**
     * Get total sales count
     */
    public long getTotalSalesCount() {
        return saleRepository.count();
    }

    /**
     * Get today's sales count
     */
    public long getTodaySalesCount() {
        return saleRepository.findTodaySales().size();
    }
}
