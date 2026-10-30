package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Supplier entity
 * Provides database operations for suppliers
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Find supplier by name
     * @param name The supplier name
     * @return Optional containing the supplier if found
     */
    Optional<Supplier> findByName(String name);

    /**
     * Find suppliers by name containing the search term
     * @param name The search term
     * @return List of matching suppliers
     */
    List<Supplier> findByNameContainingIgnoreCase(String name);

    /**
     * Find supplier by phone
     * @param phone The phone number
     * @return Optional containing the supplier if found
     */
    Optional<Supplier> findByPhone(String phone);
}
