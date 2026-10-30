package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Role;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.RoleRepository;
import com.retailpro.retailmanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for User operations
 * Contains business logic for user management
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Create new user
     */
    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if not provided
        if (user.getRole() == null) {
            Role staffRole = roleRepository.findByName("STAFF")
                    .orElseThrow(() -> new RuntimeException("Default role STAFF not found"));
            user.setRole(staffRole);
        }

        return userRepository.save(user);
    }

    /**
     * Update user
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setActive(userDetails.getActive());

        // Update role if provided
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        // Update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Initialize default admin user
     */
    public void initializeDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        role.setDescription("Full system access");
                        return roleRepository.save(role);
                    });

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@retailpro.com");
            admin.setFullName("System Administrator");
            admin.setRole(adminRole);
            admin.setPhone("1234567890");
            admin.setActive(true);

            userRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }
    }

    /**
     * Initialize default staff role
     */
    public void initializeDefaultRoles() {
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Full system access");
            roleRepository.save(adminRole);
        }

        if (!roleRepository.existsByName("STAFF")) {
            Role staffRole = new Role();
            staffRole.setName("STAFF");
            staffRole.setDescription("Limited access for daily operations");
            roleRepository.save(staffRole);
        }
    }
}
