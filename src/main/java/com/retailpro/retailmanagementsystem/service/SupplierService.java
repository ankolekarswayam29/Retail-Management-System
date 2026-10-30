package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Supplier;
import com.retailpro.retailmanagementsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Supplier operations
 * Contains business logic for supplier management
 */
@Service
@Transactional
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * Get all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    /**
     * Get supplier by ID
     */
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    /**
     * Get supplier by name
     */
    public Optional<Supplier> getSupplierByName(String name) {
        return supplierRepository.findByName(name);
    }

    /**
     * Search suppliers by name
     */
    public List<Supplier> searchSuppliers(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get supplier by phone
     */
    public Optional<Supplier> getSupplierByPhone(String phone) {
        return supplierRepository.findByPhone(phone);
    }

    /**
     * Create new supplier
     */
    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    /**
     * Update supplier
     */
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        supplier.setName(supplierDetails.getName());
        supplier.setContactPerson(supplierDetails.getContactPerson());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setCity(supplierDetails.getCity());
        supplier.setState(supplierDetails.getState());
        supplier.setPostalCode(supplierDetails.getPostalCode());
        supplier.setCountry(supplierDetails.getCountry());

        return supplierRepository.save(supplier);
    }

    /**
     * Delete supplier
     */
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        // Check if supplier has products
        if (supplier.getProducts() != null && !supplier.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete supplier with associated products");
        }

        supplierRepository.delete(supplier);
    }

    /**
     * Get total supplier count
     */
    public long getTotalSupplierCount() {
        return supplierRepository.count();
    }
}
