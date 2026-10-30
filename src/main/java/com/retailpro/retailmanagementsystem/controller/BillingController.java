package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Customer;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Sale;
import com.retailpro.retailmanagementsystem.entity.SaleItem;
import com.retailpro.retailmanagementsystem.service.BillingService;
import com.retailpro.retailmanagementsystem.service.CustomerService;
import com.retailpro.retailmanagementsystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Billing operations
 * Handles invoice creation and billing management
 */
@Controller
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    /**
     * Display billing page
     */
    @GetMapping("/billing")
    public String billingPage(Model model) {
        List<Product> products = productService.getActiveProducts();
        List<Customer> customers = customerService.getAllCustomers();
        
        model.addAttribute("products", products);
        model.addAttribute("customers", customers);
        model.addAttribute("sale", new Sale());
        model.addAttribute("saleItems", new ArrayList<SaleItem>());
        
        return "billing/create";
    }

    /**
     * Create sale/bill
     */
    @PostMapping("/billing/create")
    public String createSale(@ModelAttribute Sale sale,
                             @RequestParam(required = false) Long customerId,
                             @RequestParam(required = false) String[] productIds,
                             @RequestParam(required = false) String[] quantities,
                             RedirectAttributes redirectAttributes) {
        try {
            // Set customer if provided
            if (customerId != null) {
                Customer customer = customerService.getCustomerById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                sale.setCustomer(customer);
            }

            // Create sale items
            List<SaleItem> saleItems = new ArrayList<>();
            if (productIds != null && quantities != null) {
                for (int i = 0; i < productIds.length; i++) {
                    if (productIds[i] != null && !productIds[i].isEmpty() && 
                        quantities[i] != null && !quantities[i].isEmpty()) {
                        
                        Long productId = Long.parseLong(productIds[i]);
                        Integer quantity = Integer.parseInt(quantities[i]);
                        
                        if (quantity > 0) {
                            Product product = productService.getProductById(productId)
                                    .orElseThrow(() -> new RuntimeException("Product not found"));
                            
                            SaleItem item = new SaleItem();
                            item.setProduct(product);
                            item.setQuantity(quantity);
                            saleItems.add(item);
                        }
                    }
                }
            }

            if (saleItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please add at least one product to the bill");
                return "redirect:/billing";
            }

            // Create sale
            Sale createdSale = billingService.createSale(sale, saleItems, null);
            
            redirectAttributes.addFlashAttribute("success", "Bill created successfully");
            return "redirect:/billing/invoice/" + createdSale.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing";
        }
    }

    /**
     * Display invoice
     */
    @GetMapping("/billing/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Sale sale = billingService.getSaleById(id);
        List<SaleItem> saleItems = billingService.getSaleItems(id);
        
        model.addAttribute("sale", sale);
        model.addAttribute("saleItems", saleItems);
        
        return "billing/invoice";
    }

    /**
     * Search product for billing
     */
    @GetMapping("/billing/search-product")
    @ResponseBody
    public Product searchProduct(@RequestParam String code) {
        return productService.getProductByCode(code)
                .orElse(null);
    }
}
