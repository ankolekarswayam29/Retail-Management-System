package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Supplier;
import com.retailpro.retailmanagementsystem.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Supplier operations
 * Handles CRUD operations for suppliers
 */
@Controller
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * Display all suppliers
     */
    @GetMapping("/suppliers")
    public String listSuppliers(Model model, @RequestParam(required = false) String search) {
        List<Supplier> suppliers;
        
        if (search != null && !search.isEmpty()) {
            suppliers = supplierService.searchSuppliers(search);
            model.addAttribute("search", search);
        } else {
            suppliers = supplierService.getAllSuppliers();
        }
        
        model.addAttribute("suppliers", suppliers);
        return "supplier/list";
    }

    /**
     * Display supplier creation form
     */
    @GetMapping("/suppliers/new")
    public String showCreateForm(Model model) {
        Supplier supplier = new Supplier();
        model.addAttribute("supplier", supplier);
        return "supplier/form";
    }

    /**
     * Display supplier edit form
     */
    @GetMapping("/suppliers/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        model.addAttribute("supplier", supplier);
        return "supplier/form";
    }

    /**
     * Save supplier (create or update)
     */
    @PostMapping("/suppliers/save")
    public String saveSupplier(@ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {
        try {
            if (supplier.getId() == null) {
                supplierService.createSupplier(supplier);
                redirectAttributes.addFlashAttribute("success", "Supplier created successfully");
            } else {
                supplierService.updateSupplier(supplier.getId(), supplier);
                redirectAttributes.addFlashAttribute("success", "Supplier updated successfully");
            }
            return "redirect:/suppliers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/suppliers/new";
        }
    }

    /**
     * Delete supplier
     */
    @GetMapping("/suppliers/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }
}
