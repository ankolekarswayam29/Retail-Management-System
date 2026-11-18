
package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Category;
import com.retailpro.retailmanagementsystem.entity.Product;
import com.retailpro.retailmanagementsystem.repository.CategoryRepository;
import com.retailpro.retailmanagementsystem.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get category by ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Get category by name
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Create category
    public Category createCategory(Category category) {

        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException(
                    "Category name already exists");
        }

        return categoryRepository.save(category);
    }

    // Update category
    public Category updateCategory(
            Long id, Category categoryDetails) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (!category.getName().equals(categoryDetails.getName())
                && categoryRepository.existsByName(
                        categoryDetails.getName())) {

            throw new RuntimeException(
                    "Category name already exists");
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());

        return categoryRepository.save(category);
    }

    // Delete category
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        List<Product> productsWithCategory =
                productRepository.findByCategoryName(
                        category.getName());

        if (productsWithCategory != null
                && !productsWithCategory.isEmpty()) {

            throw new RuntimeException(
                    "Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
    }

    // Get total category count
    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }
}
