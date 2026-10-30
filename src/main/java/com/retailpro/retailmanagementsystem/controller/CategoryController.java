package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.Category;
import com.retailpro.retailmanagementsystem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for Category operations
 * Handles CRUD operations for categories
 */
@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Display all categories
     */
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "category/list";
    }

    /**
     * Display category creation form
     */
    @GetMapping("/categories/new")
    public String showCreateForm(Model model) {
        Category category = new Category();
        model.addAttribute("category", category);
        return "category/form";
    }

    /**
     * Display category edit form
     */
    @GetMapping("/categories/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return "category/form";
    }

    /**
     * Save category (create or update)
     */
    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getId() == null) {
                categoryService.createCategory(category);
                redirectAttributes.addFlashAttribute("success", "Category created successfully");
            } else {
                categoryService.updateCategory(category.getId(), category);
                redirectAttributes.addFlashAttribute("success", "Category updated successfully");
            }
            return "redirect:/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/new";
        }
    }

    /**
     * Delete category
     */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }
}
