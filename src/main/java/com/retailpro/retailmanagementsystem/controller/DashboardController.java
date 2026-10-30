package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Controller for Dashboard operations
 * Handles requests for the admin dashboard
 */
@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Display dashboard page
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Get dashboard statistics
        Map<String, Object> stats = dashboardService.getDashboardStatistics();
        model.addAttribute("stats", stats);

        // Get sales chart data
        Map<String, Object> chartData = dashboardService.getSalesChartData();
        model.addAttribute("chartData", chartData);

        // Get inventory summary
        Map<String, Object> inventorySummary = dashboardService.getInventorySummary();
        model.addAttribute("inventorySummary", inventorySummary);

        // Add user information
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        return "dashboard";
    }

    /**
     * Home page redirect to dashboard
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
