package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Role;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.RoleRepository;
import com.retailpro.retailmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for User Management operations (Admin only)
 * Handles CRUD operations, status toggling, secure password generation, and audit tracking.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Display all users with search and status filtering
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String search,
                            @RequestParam(required = false, defaultValue = "ALL") String status,
                            Model model) {

        List<User> users = userService.searchAndFilterUsers(search, status);
        List<Role> roles = roleRepository.findAll();
        List<User> allUsers = userService.getAllUsers();

        Map<String, Long> userStats = new HashMap<>();
        userStats.put("total", (long) allUsers.size());
        userStats.put("active", allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count());
        userStats.put("inactive", allUsers.stream().filter(u -> !Boolean.TRUE.equals(u.getActive())).count());
        userStats.put("pendingChange", allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getMustChangePassword())).count());

        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("userStats", userStats);

        return "user/list";
    }

    /**
     * Display user creation form
     */
    @GetMapping("/users/new")
    public String showCreateForm(Model model) {
        User user = new User();
        user.setActive(true);
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("isEdit", false);
        return "user/form";
    }

    /**
     * Admin creates a new User with automatic temporary password generation
     */
    @PostMapping("/users/create")
    public String createEmployee(@RequestParam String fullName,
                                 @RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam Long roleId,
                                 @RequestParam(defaultValue = "false") boolean customPasswordOption,
                                 @RequestParam(required = false) String customPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        String adminUsername = authentication != null ? authentication.getName() : "Administrator";

        try {
            boolean autoGenerate = !customPasswordOption;
            UserService.UserCreateResult result = userService.createEmployeeAccount(
                    fullName, username, email, phone, roleId, autoGenerate, customPassword, adminUsername
            );

            redirectAttributes.addFlashAttribute("success", "Account created successfully for " + result.user().getFullName());
            redirectAttributes.addFlashAttribute("temporaryPassword", result.temporaryPassword());
            redirectAttributes.addFlashAttribute("createdUsername", result.user().getUsername());
            redirectAttributes.addFlashAttribute("createdRole", result.user().getRole().getName());

            return "redirect:/users";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("phone", phone);
            return "redirect:/users/new";
        }
    }

    /**
     * Display user edit form
     */
    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("isEdit", true);
        return "user/form";
    }

    /**
     * Update user details
     */
    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userDetails);
            redirectAttributes.addFlashAttribute("success", "User account updated successfully.");
            return "redirect:/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/edit/" + id;
        }
    }

    /**
     * Toggle active/inactive status
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String adminUsername = authentication != null ? authentication.getName() : "Administrator";
        try {
            boolean newStatus = userService.toggleUserStatus(id, adminUsername);
            String statusLabel = newStatus ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "User account successfully " + statusLabel + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    /**
     * Admin resets a user's password to a secure temporary password
     */
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String adminUsername = authentication != null ? authentication.getName() : "Administrator";
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
            String tempPassword = userService.resetUserPassword(id, adminUsername);

            redirectAttributes.addFlashAttribute("success", "Password reset successfully for " + user.getUsername());
            redirectAttributes.addFlashAttribute("temporaryPassword", tempPassword);
            redirectAttributes.addFlashAttribute("resetUsername", user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    /**
     * Delete user account (with protection)
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

            String currentAdmin = authentication != null ? authentication.getName() : "";
            if (user.getUsername().equalsIgnoreCase(currentAdmin)) {
                throw new IllegalStateException("Security Protection: Administrators cannot delete their own active account.");
            }

            if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                long adminCount = userService.getAllUsers().stream()
                        .filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName()) && Boolean.TRUE.equals(u.getActive()))
                        .count();
                if (adminCount <= 1) {
                    throw new IllegalStateException("Security Protection: Cannot delete the last remaining active Administrator.");
                }
            }

            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User account deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }
}
