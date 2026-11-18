package com.retailpro.retailmanagementsystem.repository;

import com.retailpro.retailmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 * Provides database operations for users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * @param username The username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email The email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Count users by role name
     */
    long countByRoleName(String roleName);

    /**
     * Find users by role name
     */
    java.util.List<User> findByRoleName(String roleName);

    /**
     * Search users by keyword in full name, username, or email
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<User> searchUsers(@org.springframework.data.repository.query.Param("search") String search);
}
