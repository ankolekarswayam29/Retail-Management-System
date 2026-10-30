package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.StockTransaction;
import com.retailpro.retailmanagementsystem.service.InventoryService;
import com.retailpro.retailmanagementsystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Inventory operations
 * Handles stock management and inventory tracking
 */
@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    /**
     * Display inventory list
     */
    @GetMapping("/inventory")
    public String listInventory(Model model) {
        List<Product> products = inventoryService.getAllInventory();
        model.addAttribute("products", products);
        return "inventory/list";
    }

    /**
     * Display low stock products
     */
    @GetMapping("/inventory/low-stock")
    public String lowStockProducts(Model model) {
        List<Product> products = inventoryService.getLowStockProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Low Stock Products");
        return "inventory/list";
    }

    /**
     * Display out of stock products
     */
    @GetMapping("/inventory/out-of-stock")
    public String outOfStockProducts(Model model) {
        List<Product> products = inventoryService.getOutOfStockProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Out of Stock Products");
        return "inventory/list";
    }

    /**
     * Display stock adjustment form
     */
    @GetMapping("/inventory/adjust/{id}")
    public String showAdjustForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "inventory/adjust";
    }

    /**
     * Adjust stock
     */
    @PostMapping("/inventory/adjust")
    public String adjustStock(@RequestParam Long productId,
                              @RequestParam Integer newQuantity,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes redirectAttributes) {
        try {
            inventoryService.adjustStock(productId, newQuantity, notes, null);
            redirectAttributes.addFlashAttribute("success", "Stock adjusted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory";
    }

    /**
     * Display stock transaction history
     */
    @GetMapping("/inventory/transactions")
    public String stockTransactions(Model model) {
        List<StockTransaction> transactions = inventoryService.getRecentStockTransactions();
        model.addAttribute("transactions", transactions);
        return "inventory/transactions";
    }

    /**
     * Display stock history for a product
     */
    @GetMapping("/inventory/history/{id}")
    public String productStockHistory(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<StockTransaction> transactions = inventoryService.getProductStockHistory(id);
        
        model.addAttribute("product", product);
        model.addAttribute("transactions", transactions);
        return "inventory/history";
    }
}
