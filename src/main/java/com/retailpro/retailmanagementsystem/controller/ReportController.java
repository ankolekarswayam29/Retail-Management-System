package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.service.DashboardService;
import com.retailpro.retailmanagementsystem.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Report operations
 * Handles reports and analytics
 */
@Controller
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    @Autowired
    private SalesService salesService;

    @Autowired
    private DashboardService dashboardService;

    /**
     * Display sales report page
     */
    @GetMapping("/reports/sales")
    public String salesReport(Model model, @RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month) {
        Map<String, Object> reportData = new HashMap<>();

        if (year != null && month != null) {
            // Monthly report
            Double monthlyRevenue = salesService.calculateMonthlyRevenue(year, month);
            reportData.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
            reportData.put("year", year);
            reportData.put("month", month);
        } else {
            // Current month report
            YearMonth now = YearMonth.now();
            Double monthlyRevenue = salesService.calculateMonthlyRevenue(now.getYear(), now.getMonthValue());
            reportData.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
            reportData.put("year", now.getYear());
            reportData.put("month", now.getMonthValue());
        }

        // Get chart data
        Map<String, Object> chartData = dashboardService.getSalesChartData();
        reportData.put("chartData", chartData);

        model.addAttribute("reportData", reportData);
        return "reports/sales";
    }

    /**
     * Display inventory report page
     */
    @GetMapping("/reports/inventory")
    public String inventoryReport(Model model) {
        Map<String, Object> inventorySummary = dashboardService.getInventorySummary();
        model.addAttribute("inventorySummary", inventorySummary);
        return "reports/inventory";
    }
}
