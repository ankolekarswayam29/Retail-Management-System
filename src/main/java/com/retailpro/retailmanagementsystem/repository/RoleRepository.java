package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity
 * Provides database operations for roles
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     * @param name The role name
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role exists by name
     * @param name The role name
     * @return true if role exists, false otherwise
     */
    boolean existsByName(String name);
}
