package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Password Change operations.
 * Handles self-service password updates and first-login mandatory password updates.
 */
@Controller
public class PasswordChangeController {

    @Autowired
    private UserService userService;

    @GetMapping("/change-password")
    public String showChangePasswordPage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username).orElse(null);

        boolean mustChange = user != null && Boolean.TRUE.equals(user.getMustChangePassword());
        model.addAttribute("username", username);
        model.addAttribute("mustChange", mustChange);
        model.addAttribute("user", user);

        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        try {
            userService.changePassword(username, currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully! Your account is secured.");

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                return "redirect:/dashboard/admin";
            } else {
                return "redirect:/dashboard/user";
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            User user = userService.getUserByUsername(username).orElse(null);
            model.addAttribute("mustChange", user != null && Boolean.TRUE.equals(user.getMustChangePassword()));
            model.addAttribute("user", user);
            return "change-password";
        }
    }
}
