package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity
 * Provides database operations for customers
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by customer code
     * @param customerCode The customer code
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByCustomerCode(String customerCode);

    /**
     * Find customers by name containing the search term
     * @param name The search term
     * @return List of matching customers
     */
    List<Customer> findByNameContainingIgnoreCase(String name);

    /**
     * Find customer by phone
     * @param phone The phone number
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Find customer by email
     * @param email The email address
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Search customers by name, phone, or email
     * @param searchTerm The search term
     * @return List of matching customers
     */
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:searchTerm% OR c.phone LIKE %:searchTerm% OR c.email LIKE %:searchTerm%")
    List<Customer> searchCustomers(String searchTerm);

    /**
     * Check if customer code exists
     * @param customerCode The customer code to check
     * @return true if customer code exists, false otherwise
     */
    boolean existsByCustomerCode(String customerCode);
}
