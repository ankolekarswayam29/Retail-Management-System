package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.Role;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.RoleRepository;
import com.retailpro.retailmanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for User operations
 * Contains business logic for user management, role assignments, secure password generation and audit logging.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String CHAR_LOWER = "abcdefghjkmnpqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String CHAR_DIGITS = "23456789";
    private static final String CHAR_SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = CHAR_LOWER + CHAR_UPPER + CHAR_DIGITS + CHAR_SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public record UserCreateResult(User user, String temporaryPassword) {}

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
     * Search and filter users by keyword and status
     */
    public List<User> searchAndFilterUsers(String search, String status) {
        List<User> list;
        if (search != null && !search.trim().isEmpty()) {
            list = userRepository.searchUsers(search.trim());
        } else {
            list = userRepository.findAll();
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                list = list.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).collect(Collectors.toList());
            } else if ("INACTIVE".equalsIgnoreCase(status)) {
                list = list.stream().filter(u -> !Boolean.TRUE.equals(u.getActive())).collect(Collectors.toList());
            } else if ("PENDING_CHANGE".equalsIgnoreCase(status)) {
                list = list.stream().filter(u -> Boolean.TRUE.equals(u.getMustChangePassword())).collect(Collectors.toList());
            }
        }
        return list;
    }

    /**
     * Generate a cryptographically strong 12-character temporary password
     */
    public String generateSecureTemporaryPassword() {
        StringBuilder sb = new StringBuilder(12);
        // Ensure at least one character from each set
        sb.append(CHAR_UPPER.charAt(RANDOM.nextInt(CHAR_UPPER.length())));
        sb.append(CHAR_LOWER.charAt(RANDOM.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_DIGITS.charAt(RANDOM.nextInt(CHAR_DIGITS.length())));
        sb.append(CHAR_SPECIAL.charAt(RANDOM.nextInt(CHAR_SPECIAL.length())));

        for (int i = 4; i < 12; i++) {
            sb.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle characters
        List<Character> chars = sb.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(chars, RANDOM);
        StringBuilder result = new StringBuilder();
        chars.forEach(result::append);
        return result.toString();
    }

    /**
     * Admin creates a new User account
     */
    public UserCreateResult createEmployeeAccount(String fullName, String username, String email, String phone,
                                                  Long roleId, boolean autoGeneratePassword, String customPassword,
                                                  String adminUsername) {

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        String cleanUsername = username.trim();
        if (cleanUsername.length() < 3 || cleanUsername.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters.");
        }
        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username '" + cleanUsername + "' is already taken.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String cleanEmail = email.trim();
        if (!cleanEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' is already registered to another user.");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full employee name is required.");
        }

        Role role;
        if (roleId != null) {
            role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected role does not exist."));
        } else {
            role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role USER not found in database."));
        }

        String rawPassword;
        boolean isTemporary;
        if (autoGeneratePassword || customPassword == null || customPassword.trim().isEmpty()) {
            rawPassword = generateSecureTemporaryPassword();
            isTemporary = true;
        } else {
            if (customPassword.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters.");
            }
            rawPassword = customPassword;
            isTemporary = false;
        }

        User user = new User();
        user.setUsername(cleanUsername);
        user.setFullName(fullName.trim());
        user.setEmail(cleanEmail);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setTemporaryPassword(isTemporary);
        user.setMustChangePassword(true); // Mandatory change on first login

        User savedUser = userRepository.save(user);

        log.info("[AUDIT] User '{}' (ID: {}) created with role '{}' by administrator '{}'. Temporary password: {}",
                savedUser.getUsername(), savedUser.getId(), role.getName(), adminUsername, isTemporary);

        return new UserCreateResult(savedUser, rawPassword);
    }

    /**
     * Admin resets a user's password
     */
    public String resetUserPassword(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String tempPassword = generateSecureTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        user.setTemporaryPassword(true);
        userRepository.save(user);

        log.info("[AUDIT] Password for user '{}' (ID: {}) reset by administrator '{}'. Mandatory password change enforced.",
                user.getUsername(), user.getId(), adminUsername);

        return tempPassword;
    }

    /**
     * Toggle active status of a user (with protection against disabling the last admin or self)
     */
    public boolean toggleUserStatus(Long userId, String currentAdminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getUsername().equalsIgnoreCase(currentAdminUsername)) {
            throw new IllegalStateException("Security Protection: Administrators cannot deactivate their own active account.");
        }

        if (Boolean.TRUE.equals(user.getActive()) && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            long activeAdmins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName()) && Boolean.TRUE.equals(u.getActive()))
                    .count();
            if (activeAdmins <= 1) {
                throw new IllegalStateException("Security Protection: Cannot deactivate the last remaining active Administrator.");
            }
        }

        boolean newStatus = !Boolean.TRUE.equals(user.getActive());
        user.setActive(newStatus);
        userRepository.save(user);

        log.info("[AUDIT] User '{}' (ID: {}) status updated to active={} by administrator '{}'.",
                user.getUsername(), user.getId(), newStatus, currentAdminUsername);
        return newStatus;
    }

    /**
     * User changes their own password
     */
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User account not found."));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException("Account is inactive. Contact your administrator.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("The current password entered is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setTemporaryPassword(false);
        userRepository.save(user);

        log.info("[AUDIT] User '{}' successfully updated their password.", username);
    }

    /**
     * Record last login timestamp upon successful authentication
     */
    public void recordLoginSuccess(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * Legacy create user (used by existing forms or services)
     */
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role USER not found"));
            user.setRole(userRole);
        }

        return userRepository.save(user);
    }

    /**
     * Update user details
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if email changed and if duplicate
        if (!user.getEmail().equalsIgnoreCase(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDetails.getEmail() + "' is already in use.");
        }

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        if (userDetails.getActive() != null) {
            user.setActive(userDetails.getActive());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            user.setMustChangePassword(false);
        }

        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }
}
