package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.security.AdminBootstrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * SetupController
 * Handles one-time bootstrap setup for the primary Administrator account.
 * Permanently locks once an Administrator exists.
 */
@Controller
public class SetupController {

    @Autowired
    private AdminBootstrapService adminBootstrapService;

    @GetMapping("/setup")
    public String showSetupPage(Model model, RedirectAttributes redirectAttributes) {
        if (!adminBootstrapService.isBootstrapRequired()) {
            redirectAttributes.addFlashAttribute("error", "Initial setup has already been completed. Endpoint is permanently locked.");
            return "redirect:/login";
        }
        return "setup";
    }

    @PostMapping("/setup")
    public String processSetup(@RequestParam String secretKey,
                               @RequestParam String username,
                               @RequestParam String fullName,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (!adminBootstrapService.isBootstrapRequired()) {
            redirectAttributes.addFlashAttribute("error", "Initial setup has already been completed. Endpoint is permanently locked.");
            return "redirect:/login";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            return "setup";
        }

        try {
            adminBootstrapService.performBootstrapSetup(secretKey, username, password, email, fullName);
            redirectAttributes.addFlashAttribute("success", "Administrator account provisioned successfully. You may now log in.");
            return "redirect:/login?setupSuccess=true";
        } catch (SecurityException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            return "setup";
        }
    }
}
