package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.repository.CustomerRepository;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;
import com.retailpro.retailmanagementsystem.repository.SaleRepository;
import com.retailpro.retailmanagementsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for Dashboard operations
 * Provides statistics and data for the admin dashboard
 */
@Service
@Transactional
public class DashboardService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SaleRepository saleRepository;

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total counts
        stats.put("totalProducts", productRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalSuppliers", supplierRepository.count());
        stats.put("totalSales", saleRepository.count());

        // Today's statistics
        List<Sale> todaySales = saleRepository.findTodaySales();
        stats.put("todaySalesCount", (long) todaySales.size());
        stats.put("todayRevenue", saleRepository.calculateTodayRevenue() != null ? 
                saleRepository.calculateTodayRevenue() : 0.0);

        // Monthly statistics
        YearMonth now = YearMonth.now();
        LocalDateTime startOfMonth = now.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = now.atEndOfMonth().atTime(23, 59, 59);
        stats.put("monthlyRevenue", saleRepository.calculateTotalRevenue(startOfMonth, endOfMonth) != null ?
                saleRepository.calculateTotalRevenue(startOfMonth, endOfMonth) : 0.0);

        // Low stock products
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        stats.put("lowStockCount", (long) lowStockProducts.size());
        stats.put("lowStockProducts", lowStockProducts);

        // Recent sales
        List<Sale> recentSales = saleRepository.findRecentSales();
        stats.put("recentSales", recentSales.size() > 5 ? recentSales.subList(0, 5) : recentSales);

        return stats;
    }

    /**
     * Get sales data for charts
     */
    public Map<String, Object> getSalesChartData() {
        Map<String, Object> chartData = new HashMap<>();

        // Last 7 days sales
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Sale> last7DaysSales = saleRepository.findBySaleDateBetween(sevenDaysAgo, LocalDateTime.now());
        chartData.put("last7DaysSales", last7DaysSales);

        // Monthly sales for current year
        Map<Integer, Double> monthlySales = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            Double revenue = saleRepository.calculateTotalRevenue(
                    LocalDateTime.of(2024, month, 1, 0, 0),
                    LocalDateTime.of(2024, month, YearMonth.of(2024, month).lengthOfMonth(), 23, 59, 59)
            );
            monthlySales.put(month, revenue != null ? revenue : 0.0);
        }
        chartData.put("monthlySales", monthlySales);

        return chartData;
    }

    /**
     * Get inventory summary
     */
    public Map<String, Object> getInventorySummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Product> allProducts = productRepository.findAll();
        long totalStock = allProducts.stream()
                .mapToLong(Product::getStockQuantity)
                .sum();

        long outOfStockCount = allProducts.stream()
                .filter(Product::isOutOfStock)
                .count();

        long lowStockCount = allProducts.stream()
                .filter(Product::isLowStock)
                .count();

        summary.put("totalStock", totalStock);
        summary.put("outOfStockCount", outOfStockCount);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalProducts", (long) allProducts.size());

        return summary;
    }
}
