package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.repository.CustomerRepository;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;
import com.retailpro.retailmanagementsystem.repository.SaleItemRepository;
import com.retailpro.retailmanagementsystem.repository.SaleRepository;
import com.retailpro.retailmanagementsystem.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void testGetDashboardStatistics() {
        when(productRepository.count()).thenReturn(150L);
        when(customerRepository.count()).thenReturn(45L);
        when(supplierRepository.count()).thenReturn(12L);
        when(saleRepository.count()).thenReturn(320L);
        when(saleRepository.calculateAllTimeRevenue()).thenReturn(45800.0);
        when(saleRepository.findTodaySales()).thenReturn(Collections.emptyList());
        when(saleRepository.calculateTodayRevenue()).thenReturn(0.0);
        when(saleRepository.calculateTotalRevenue(any(), any())).thenReturn(5200.0);
        when(productRepository.findLowStockProducts()).thenReturn(Collections.emptyList());
        when(saleRepository.findRecentSales()).thenReturn(Collections.emptyList());
        List<Object[]> revCostList = new java.util.ArrayList<>();
        revCostList.add(new Object[]{5000.0, 3000.0});
        when(saleItemRepository.calculateRevenueAndExpense()).thenReturn(revCostList);

        Map<String, Object> stats = dashboardService.getDashboardStatistics();

        assertNotNull(stats);
        assertEquals(150L, stats.get("totalProducts"));
        assertEquals(45L, stats.get("totalCustomers"));
        assertEquals(45800.0, stats.get("totalRevenue"));
        assertEquals(2000.0, stats.get("totalProfit"));
        assertEquals(40.0, stats.get("profitMargin"));
    }

    @Test
    void testGetAnalyticsByPeriod() {
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(Collections.emptyList());
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> analytics = dashboardService.getAnalyticsByPeriod("month");

        assertNotNull(analytics);
        assertEquals("month", analytics.get("period"));
        assertTrue(analytics.containsKey("monthlyLabels"));
        assertTrue(analytics.containsKey("monthlyRevenue"));
        assertTrue(analytics.containsKey("dailyLabels"));
        assertTrue(analytics.containsKey("dailyRevenue"));
        assertTrue(analytics.containsKey("categoryLabels"));
        assertTrue(analytics.containsKey("topProductLabels"));
        assertTrue(analytics.containsKey("inventoryLabels"));
        assertTrue(analytics.containsKey("profitExpenseLabels"));
    }
}
