package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.entity.SaleItem;
import com.retailpro.retailmanagementsystem.repository.CustomerRepository;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;
import com.retailpro.retailmanagementsystem.repository.SaleItemRepository;
import com.retailpro.retailmanagementsystem.repository.SaleRepository;
import com.retailpro.retailmanagementsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Dashboard operations
 * Provides statistics and analytics data for executive dashboards and charts
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

    @Autowired
    private SaleItemRepository saleItemRepository;

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

        // All-time revenue
        Double totalRev = saleRepository.calculateAllTimeRevenue();
        stats.put("totalRevenue", totalRev != null ? totalRev : 0.0);

        // Today's statistics
        List<Sale> todaySales = saleRepository.findTodaySales();
        stats.put("todaySalesCount", (long) todaySales.size());
        Double todayRev = saleRepository.calculateTodayRevenue();
        stats.put("todayRevenue", todayRev != null ? todayRev : 0.0);

        // Monthly statistics (current month)
        YearMonth now = YearMonth.now();
        LocalDateTime startOfMonth = now.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = now.atEndOfMonth().atTime(LocalTime.MAX);
        Double monthlyRev = saleRepository.calculateTotalRevenue(startOfMonth, endOfMonth);
        stats.put("monthlyRevenue", monthlyRev != null ? monthlyRev : 0.0);

        // Low stock products
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        stats.put("lowStockCount", (long) lowStockProducts.size());
        stats.put("lowStockProducts", lowStockProducts);

        // Recent sales (up to 8)
        List<Sale> recentSales = saleRepository.findRecentSales();
        stats.put("recentSales", recentSales.size() > 8 ? recentSales.subList(0, 8) : recentSales);

        // Revenue vs Cost all time
        List<Object[]> revCost = saleItemRepository.calculateRevenueAndExpense();
        double totalCost = 0.0;
        double totalRevenueFromItems = 0.0;
        if (revCost != null && !revCost.isEmpty() && revCost.get(0) != null) {
            Object[] row = revCost.get(0);
            if (row[0] != null) totalRevenueFromItems = ((Number) row[0]).doubleValue();
            if (row[1] != null) totalCost = ((Number) row[1]).doubleValue();
        }
        double grossProfit = Math.max(0.0, totalRevenueFromItems - totalCost);
        double margin = totalRevenueFromItems > 0 ? (grossProfit / totalRevenueFromItems) * 100 : 0.0;
        stats.put("totalExpense", totalCost);
        stats.put("totalProfit", grossProfit);
        stats.put("profitMargin", Math.round(margin * 10.0) / 10.0);

        return stats;
    }

    /**
     * Get sales and analytics data for initial chart rendering
     */
    public Map<String, Object> getSalesChartData() {
        return getAnalyticsByPeriod("month");
    }

    /**
     * Get analytics filtered by period: "today", "week", "month", "year"
     */
    public Map<String, Object> getAnalyticsByPeriod(String period) {
        if (period == null || period.trim().isEmpty()) {
            period = "month";
        }
        period = period.toLowerCase();

        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime;
        LocalDateTime endDateTime = LocalDateTime.now();

        switch (period) {
            case "today":
                startDateTime = today.atStartOfDay();
                break;
            case "week":
                startDateTime = today.minusDays(6).atStartOfDay();
                break;
            case "year":
                startDateTime = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
                break;
            case "month":
            default:
                startDateTime = today.withDayOfMonth(1).atStartOfDay();
                period = "month";
                break;
        }

        List<Sale> salesInPeriod = saleRepository.findBySaleDateBetween(startDateTime, endDateTime);

        // Calculate period metrics
        double periodRevenue = salesInPeriod.stream()
                .map(Sale::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        long periodSalesCount = salesInPeriod.size();

        // Monthly sales for current year (Line Chart)
        int currentYear = today.getYear();
        List<String> monthLabels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        List<Double> monthlyRevenueData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            LocalDateTime mStart = LocalDateTime.of(currentYear, m, 1, 0, 0);
            LocalDateTime mEnd = LocalDateTime.of(currentYear, m, YearMonth.of(currentYear, m).lengthOfMonth(), 23, 59, 59);
            Double rev = saleRepository.calculateTotalRevenue(mStart, mEnd);
            monthlyRevenueData.add(rev != null ? rev : 0.0);
        }

        // Daily sales breakdown for period (Bar Chart)
        List<String> dailyLabels = new ArrayList<>();
        List<Double> dailyRevenueData = new ArrayList<>();
        List<Long> dailySalesCountData = new ArrayList<>();

        if ("today".equals(period)) {
            // Hourly breakdown for today
            for (int h = 8; h <= 21; h += 2) {
                dailyLabels.add(String.format("%02d:00", h));
                int hourStart = h;
                int hourEnd = h + 2;
                double hRev = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().getHour() >= hourStart && s.getSaleDate().getHour() < hourEnd)
                        .map(Sale::getTotalAmount)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();
                long hCount = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().getHour() >= hourStart && s.getSaleDate().getHour() < hourEnd)
                        .count();
                dailyRevenueData.add(hRev);
                dailySalesCountData.add(hCount);
            }
        } else if ("week".equals(period)) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE (dd)");
            for (int d = 6; d >= 0; d--) {
                LocalDate date = today.minusDays(d);
                dailyLabels.add(date.format(df));
                double dRev = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().toLocalDate().isEqual(date))
                        .map(Sale::getTotalAmount)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();
                long dCount = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().toLocalDate().isEqual(date))
                        .count();
                dailyRevenueData.add(dRev);
                dailySalesCountData.add(dCount);
            }
        } else {
            // Last 7 days or current month days
            DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd");
            int daysBack = "year".equals(period) ? 30 : Math.min(today.getDayOfMonth(), 14);
            LocalDate loopStart = today.minusDays(daysBack - 1);
            for (int i = 0; i < daysBack; i++) {
                LocalDate date = loopStart.plusDays(i);
                dailyLabels.add(date.format(df));
                double dRev = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().toLocalDate().isEqual(date))
                        .map(Sale::getTotalAmount)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();
                long dCount = salesInPeriod.stream()
                        .filter(s -> s.getSaleDate().toLocalDate().isEqual(date))
                        .count();
                dailyRevenueData.add(dRev);
                dailySalesCountData.add(dCount);
            }
        }

        // Category-wise sales (Doughnut Chart)
        List<Object[]> categoryData = saleItemRepository.findCategorySalesSummary();
        List<String> categoryLabels = new ArrayList<>();
        List<Double> categoryValues = new ArrayList<>();
        if (categoryData != null && !categoryData.isEmpty()) {
            for (Object[] row : categoryData) {
                if (row[0] != null) {
                    categoryLabels.add(row[0].toString());
                    categoryValues.add(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0);
                }
            }
        }

        // Top-selling products (Horizontal Bar Chart)
        List<Object[]> topProdData = saleItemRepository.findTopSellingProducts();
        List<String> topProductLabels = new ArrayList<>();
        List<Long> topProductQuantities = new ArrayList<>();
        List<Double> topProductRevenues = new ArrayList<>();
        if (topProdData != null && !topProdData.isEmpty()) {
            int limit = Math.min(topProdData.size(), 6);
            for (int i = 0; i < limit; i++) {
                Object[] row = topProdData.get(i);
                if (row[0] != null) {
                    topProductLabels.add(row[0].toString());
                    topProductQuantities.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
                    topProductRevenues.add(row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                }
            }
        }

        // Inventory health summary (Stock Analysis Chart)
        Map<String, Object> inventorySummary = getInventorySummary();

        // Profit & Expense breakdown
        List<Object[]> revCost = saleItemRepository.calculateRevenueAndExpense();
        double totalExpense = 0.0;
        double totalItemRevenue = 0.0;
        if (revCost != null && !revCost.isEmpty() && revCost.get(0) != null) {
            Object[] row = revCost.get(0);
            if (row[0] != null) totalItemRevenue = ((Number) row[0]).doubleValue();
            if (row[1] != null) totalExpense = ((Number) row[1]).doubleValue();
        }
        double totalProfit = Math.max(0.0, totalItemRevenue - totalExpense);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("period", period);
        analytics.put("periodRevenue", Math.round(periodRevenue * 100.0) / 100.0);
        analytics.put("periodSalesCount", periodSalesCount);

        // Chart 1: Monthly Revenue (Line)
        analytics.put("monthlyLabels", monthLabels);
        analytics.put("monthlyRevenue", monthlyRevenueData);

        // Chart 2: Daily Sales (Bar)
        analytics.put("dailyLabels", dailyLabels);
        analytics.put("dailyRevenue", dailyRevenueData);
        analytics.put("dailyCount", dailySalesCountData);

        // Chart 3: Category Doughnut
        analytics.put("categoryLabels", categoryLabels);
        analytics.put("categoryValues", categoryValues);

        // Chart 4: Top Products (Bar)
        analytics.put("topProductLabels", topProductLabels);
        analytics.put("topProductQuantities", topProductQuantities);
        analytics.put("topProductRevenues", topProductRevenues);

        // Chart 5: Inventory Stock Status
        analytics.put("inventoryLabels", List.of("Healthy Stock", "Low Stock Alert", "Out of Stock"));
        long totalProducts = (Long) inventorySummary.get("totalProducts");
        long lowStock = (Long) inventorySummary.get("lowStockCount");
        long outOfStock = (Long) inventorySummary.get("outOfStockCount");
        long healthyStock = Math.max(0, totalProducts - lowStock - outOfStock);
        analytics.put("inventoryValues", List.of(healthyStock, lowStock, outOfStock));

        // Chart 6: Profit & Expense comparison
        analytics.put("profitExpenseLabels", List.of("Total Revenue", "Cost of Goods (Expense)", "Gross Profit"));
        analytics.put("profitExpenseValues", List.of(
                Math.round(totalItemRevenue * 100.0) / 100.0,
                Math.round(totalExpense * 100.0) / 100.0,
                Math.round(totalProfit * 100.0) / 100.0
        ));

        return analytics;
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

        double totalInventoryValue = allProducts.stream()
                .mapToDouble(p -> (p.getSellingPrice() != null ? p.getSellingPrice().doubleValue() : 0.0) * p.getStockQuantity())
                .sum();

        summary.put("totalStock", totalStock);
        summary.put("outOfStockCount", outOfStockCount);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalProducts", (long) allProducts.size());
        summary.put("totalInventoryValue", Math.round(totalInventoryValue * 100.0) / 100.0);

        return summary;
    }

    /**
     * Get tailored dashboard data for employees/users
     */
    public Map<String, Object> getUserDashboardData(String username) {
        Map<String, Object> data = new HashMap<>();

        // Today's business performance
        List<Sale> todaySales = saleRepository.findTodaySales();
        Double todayRevenue = saleRepository.calculateTodayRevenue();
        long todaySalesCount = todaySales != null ? todaySales.size() : 0;
        double revenueVal = todayRevenue != null ? todayRevenue : 0.0;
        double avgSale = todaySalesCount > 0 ? (revenueVal / todaySalesCount) : 0.0;

        data.put("todaySalesCount", todaySalesCount);
        data.put("todayRevenue", revenueVal);
        data.put("todayAverageOrder", Math.round(avgSale * 100.0) / 100.0);

        // Recent sales orders
        List<Sale> recentSales = saleRepository.findRecentSales();
        data.put("recentSales", recentSales.size() > 10 ? recentSales.subList(0, 10) : recentSales);

        // Top / frequently sold products
        List<Object[]> topSoldRaw = saleItemRepository.findTopSellingProducts();
        List<Map<String, Object>> topProducts = new ArrayList<>();
        if (topSoldRaw != null) {
            for (int i = 0; i < Math.min(6, topSoldRaw.size()); i++) {
                Object[] row = topSoldRaw.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("name", row[0]);
                item.put("quantity", row[1]);
                item.put("revenue", row[2]);
                topProducts.add(item);
            }
        }
        data.put("topProducts", topProducts);

        // Low stock products alerts
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        data.put("lowStockCount", (long) lowStockProducts.size());
        data.put("lowStockProducts", lowStockProducts.size() > 8 ? lowStockProducts.subList(0, 8) : lowStockProducts);

        // Active catalog items for quick search and lookup
        List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .limit(50)
                .collect(Collectors.toList());
        data.put("catalogProducts", activeProducts);
        data.put("totalProducts", productRepository.count());

        return data;
    }

    /**
     * Real-time live statistics for polling updates
     */
    public Map<String, Object> getLiveRealtimeStats() {
        Map<String, Object> live = new HashMap<>();

        List<Sale> todaySales = saleRepository.findTodaySales();
        Double todayRevenue = saleRepository.calculateTodayRevenue();
        long todaySalesCount = todaySales != null ? todaySales.size() : 0;
        double revenueVal = todayRevenue != null ? todayRevenue : 0.0;

        live.put("todaySalesCount", todaySalesCount);
        live.put("todayRevenue", revenueVal);
        live.put("totalProducts", productRepository.count());
        live.put("totalSales", saleRepository.count());

        List<Product> lowStock = productRepository.findLowStockProducts();
        live.put("lowStockCount", lowStock.size());

        List<Map<String, Object>> lowStockList = new ArrayList<>();
        for (int i = 0; i < Math.min(5, lowStock.size()); i++) {
            Product p = lowStock.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("code", p.getProductCode());
            item.put("stock", p.getStockQuantity());
            item.put("minStock", p.getMinimumStock());
            lowStockList.add(item);
        }
        live.put("lowStockProducts", lowStockList);

        List<Sale> recent = saleRepository.findRecentSales();
        List<Map<String, Object>> recentList = new ArrayList<>();
        int limit = Math.min(6, recent.size());
        for (int i = 0; i < limit; i++) {
            Sale s = recent.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("invoiceNumber", s.getInvoiceNumber());
            item.put("customer", s.getCustomer() != null ? s.getCustomer().getName() : "Walk-in Customer");
            item.put("amount", s.getTotalAmount());
            item.put("date", s.getSaleDate() != null ? s.getSaleDate().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");
            recentList.add(item);
        }
        live.put("recentSales", recentList);
        live.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        return live;
    }
}
