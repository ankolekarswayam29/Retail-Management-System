package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Category entity
 * Provides database operations for categories
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     * @param name The category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category name exists
     * @param name The category name to check
     * @return true if name exists, false otherwise
     */
    boolean existsByName(String name);
}
