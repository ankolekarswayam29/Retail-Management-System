package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Customer;
import com.retailpro.retailmanagementsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Customer operations
 * Contains business logic for customer management
 */
@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Get customer by ID
     */
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * Get customer by customer code
     */
    public Optional<Customer> getCustomerByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode);
    }

    /**
     * Search customers by name, phone, or email
     */
    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.searchCustomers(searchTerm);
    }

    /**
     * Get customer by phone
     */
    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    /**
     * Create new customer
     */
    public Customer createCustomer(Customer customer) {
        // Check if customer code already exists
        if (customerRepository.existsByCustomerCode(customer.getCustomerCode())) {
            throw new RuntimeException("Customer code already exists");
        }

        // Set default values
        if (customer.getTotalPurchases() == null) {
            customer.setTotalPurchases(java.math.BigDecimal.ZERO);
        }
        if (customer.getTotalVisits() == null) {
            customer.setTotalVisits(0);
        }

        return customerRepository.save(customer);
    }

    /**
     * Update customer
     */
    public Customer updateCustomer(Long id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setState(customerDetails.getState());
        customer.setPostalCode(customerDetails.getPostalCode());

        return customerRepository.save(customer);
    }

    /**
     * Delete customer
     */
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if customer has sales
        if (customer.getSales() != null && !customer.getSales().isEmpty()) {
            throw new RuntimeException("Cannot delete customer with associated sales");
        }

        customerRepository.delete(customer);
    }

    /**
     * Update customer purchase statistics
     */
    public Customer updatePurchaseStats(Long customerId, java.math.BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.updatePurchaseStats(amount);
        return customerRepository.save(customer);
    }

    /**
     * Get total customer count
     */
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }

    /**
     * Generate unique customer code
     */
    public String generateCustomerCode() {
        long count = customerRepository.count();
        return "CUST-" + String.format("%04d", count + 1);
    }
}
