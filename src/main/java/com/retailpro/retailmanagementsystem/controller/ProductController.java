package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Category;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.entity.Supplier;
import com.retailpro.retailmanagementsystem.service.CategoryService;
import com.retailpro.retailmanagementsystem.service.ProductService;
import com.retailpro.retailmanagementsystem.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Product operations
 * Handles CRUD operations for products
 */
@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SupplierService supplierService;

    /**
     * Display all products
     */
    @GetMapping("/products")
    public String listProducts(Model model, @RequestParam(required = false) String search) {
        List<Product> products;
        
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("search", search);
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("products", products);
        return "product/list";
    }

    /**
     * Display product creation form
     */
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        Product product = new Product();
        List<Category> categories = categoryService.getAllCategories();
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("suppliers", suppliers);
        return "product/form";
    }

    /**
     * Display product edit form
     */
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        List<Category> categories = categoryService.getAllCategories();
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("suppliers", suppliers);
        return "product/form";
    }

    /**
     * Save product (create or update)
     */
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        try {
            if (product.getId() == null) {
                productService.createProduct(product);
                redirectAttributes.addFlashAttribute("success", "Product created successfully");
            } else {
                productService.updateProduct(product.getId(), product);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            }
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/new";
        }
    }

    /**
     * Delete product
     */
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    /**
     * View product details
     */
    @GetMapping("/products/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "product/details";
    }
}
