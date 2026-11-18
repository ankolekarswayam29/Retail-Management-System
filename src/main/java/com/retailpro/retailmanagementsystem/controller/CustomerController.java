package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Customer;
import com.retailpro.retailmanagementsystem.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Customer operations
 * Handles CRUD operations for customers
 */
@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * Display all customers
     */
    @GetMapping("/customers")
    public String listCustomers(Model model, @RequestParam(required = false) String search) {
        List<Customer> customers;
        
        if (search != null && !search.isEmpty()) {
            customers = customerService.searchCustomers(search);
            model.addAttribute("search", search);
        } else {
            customers = customerService.getAllCustomers();
        }
        
        model.addAttribute("customers", customers);
        return "customer/list";
    }

    /**
     * Display customer creation form
     */
    @GetMapping("/customers/new")
    public String showCreateForm(Model model) {
        Customer customer = new Customer();
        customer.setCustomerCode(customerService.generateCustomerCode());
        model.addAttribute("customer", customer);
        return "customer/form";
    }

    /**
     * Display customer edit form
     */
    @GetMapping("/customers/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        return "customer/form";
    }

    /**
     * Save customer (create or update)
     */
    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            if (customer.getId() == null) {
                customerService.createCustomer(customer);
                redirectAttributes.addFlashAttribute("success", "Customer created successfully");
            } else {
                customerService.updateCustomer(customer.getId(), customer);
                redirectAttributes.addFlashAttribute("success", "Customer updated successfully");
            }
            return "redirect:/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers/new";
        }
    }

    /**
     * Delete customer (Admin only)
     */
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }
}
