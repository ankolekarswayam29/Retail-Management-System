package com.retailpro.retailmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Authentication operations
 * Handles login and logout pages
 */
@Controller
public class AuthController {

    /**
     * Display login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "login";
    }

    /**
     * Display access denied page
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("error", "Access Denied");
        return "access-denied";
    }
}
