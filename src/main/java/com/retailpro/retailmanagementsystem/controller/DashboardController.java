package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.UserRepository;
import com.retailpro.retailmanagementsystem.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Common dashboard router: inspects authenticated database role and redirects to appropriate dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/dashboard/admin";
        }

        return "redirect:/dashboard/user";
    }

    /**
     * Admin Dashboard View
     */
    @GetMapping({"/dashboard/admin", "/admin/dashboard"})
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model, Authentication authentication) {
        Map<String, Object> stats = dashboardService.getDashboardStatistics();
        Map<String, Object> chartData = dashboardService.getSalesChartData();
        Map<String, Object> inventorySummary = dashboardService.getInventorySummary();

        String username = authentication != null ? authentication.getName() : "Administrator";
        User user = userRepository.findByUsername(username).orElse(null);

        model.addAttribute("stats", stats);
        model.addAttribute("chartData", chartData);
        model.addAttribute("inventorySummary", inventorySummary);
        model.addAttribute("username", username);
        model.addAttribute("employeeName", user != null ? user.getFullName() : username);
        model.addAttribute("role", "ADMIN");
        model.addAttribute("user", user);

        return "dashboard/admin";
    }

    /**
     * User / Employee Dashboard View
     */
    @GetMapping({"/dashboard/user", "/user/dashboard"})
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String userDashboard(Model model, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Staff Member";
        User user = userRepository.findByUsername(username).orElse(null);

        Map<String, Object> userData = dashboardService.getUserDashboardData(username);
        Map<String, Object> stats = dashboardService.getDashboardStatistics();

        model.addAttribute("userData", userData);
        model.addAttribute("stats", stats);
        model.addAttribute("username", username);
        model.addAttribute("employeeName", user != null ? user.getFullName() : username);
        model.addAttribute("role", user != null && user.getRole() != null ? user.getRole().getName() : "USER");
        model.addAttribute("user", user);

        return "dashboard/user";
    }

    /**
     * REST Endpoint for periodic live statistics polling
     */
    @GetMapping("/api/dashboard/live-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLiveStats() {
        Map<String, Object> liveStats = dashboardService.getLiveRealtimeStats();
        return ResponseEntity.ok(liveStats);
    }

    /**
     * REST Endpoint for live interactive dashboard analytics with period filters
     */
    @GetMapping("/api/dashboard/analytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics(@RequestParam(defaultValue = "month") String period) {
        Map<String, Object> analytics = dashboardService.getAnalyticsByPeriod(period);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
