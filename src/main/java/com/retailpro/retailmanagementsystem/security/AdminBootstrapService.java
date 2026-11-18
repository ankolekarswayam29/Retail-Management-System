package com.retailpro.retailmanagementsystem.security;

import com.retailpro.retailmanagementsystem.entity.Role;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.RoleRepository;
import com.retailpro.retailmanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AdminBootstrapService
 * Handles secure first-time system bootstrap and initial admin provisioning.
 * Complies with strict zero-hardcoded-credentials and single-use setup policy.
 */
@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${retailpro.security.bootstrap.secret-key:${RETAILPRO_ADMIN_SECRET:}}")
    private String configuredSecretKey;

    @Value("${retailpro.security.bootstrap.initial-username:${RETAILPRO_ADMIN_USERNAME:}}")
    private String envAdminUsername;

    @Value("${retailpro.security.bootstrap.initial-password:${RETAILPRO_ADMIN_PASSWORD:}}")
    private String envAdminPassword;

    @Value("${retailpro.security.bootstrap.initial-email:${RETAILPRO_ADMIN_EMAIL:admin@retailpro.com}}")
    private String envAdminEmail;

    @Value("${retailpro.security.bootstrap.initial-fullname:${RETAILPRO_ADMIN_NAME:System Administrator}}")
    private String envAdminFullName;

    // Generated runtime secret if no env var was provided, to prevent unauthenticated access
    private String runtimeFallbackSecret;

    /**
     * Check if the system has not been initialized with any Administrator account
     */
    public boolean isBootstrapRequired() {
        return userRepository.countByRoleName("ADMIN") == 0;
    }

    /**
     * Constant-time comparison of the provided activation key
     */
    public boolean isActivationKeyValid(String providedKey) {
        if (providedKey == null || providedKey.trim().isEmpty()) {
            return false;
        }

        String targetSecret = getEffectiveSecretKey();
        if (targetSecret == null || targetSecret.trim().isEmpty()) {
            return false;
        }

        return MessageDigest.isEqual(
                providedKey.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                targetSecret.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private synchronized String getEffectiveSecretKey() {
        if (configuredSecretKey != null && !configuredSecretKey.trim().isEmpty()) {
            return configuredSecretKey.trim();
        }
        if (runtimeFallbackSecret == null) {
            byte[] bytes = new byte[24];
            new SecureRandom().nextBytes(bytes);
            runtimeFallbackSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            log.warn("================================================================================");
            log.warn("RETAILPRO SECURITY NOTICE: RETAILPRO_ADMIN_SECRET was not configured in environment.");
            log.warn("Generated a one-time runtime activation key for initial /setup:");
            log.warn(">>> {} <<<", runtimeFallbackSecret);
            log.warn("Set RETAILPRO_ADMIN_SECRET in environment for production deployments.");
            log.warn("================================================================================");
        }
        return runtimeFallbackSecret;
    }

    /**
     * Initialize base roles and optionally bootstrap admin from environment variables
     */
    @Transactional
    public void initializeSystem() {
        // Ensure default roles exist
        ensureRoleExists("ADMIN", "Full system administrator access");
        ensureRoleExists("USER", "Standard employee and retail operations access");

        if (!isBootstrapRequired()) {
            log.info("[BOOTSTRAP] Administrator account already present. Bootstrap setup is permanently locked.");
            return;
        }

        // Check if environment variables provide initial credentials
        if (envAdminUsername != null && !envAdminUsername.trim().isEmpty() &&
            envAdminPassword != null && !envAdminPassword.trim().isEmpty()) {

            log.info("[BOOTSTRAP] Provisioning initial Administrator account from environment configuration...");
            createAdministrator(
                    envAdminUsername.trim(),
                    envAdminPassword.trim(),
                    envAdminEmail != null ? envAdminEmail.trim() : "admin@retailpro.com",
                    envAdminFullName != null ? envAdminFullName.trim() : "System Administrator"
            );
            log.info("[BOOTSTRAP] Initial Administrator account '{}' provisioned successfully. Bootstrap locked.", envAdminUsername.trim());
        } else {
            log.warn("[BOOTSTRAP] System requires initial administrator setup.");
            log.warn("[BOOTSTRAP] Navigate to /setup to initialize the administrator account using your secret key.");
            // Trigger generation and display of fallback key if needed
            getEffectiveSecretKey();
        }
    }

    /**
     * Create the first administrator through bootstrap setup
     */
    @Transactional
    public void performBootstrapSetup(String activationKey, String username, String password, String email, String fullName) {
        if (!isBootstrapRequired()) {
            throw new SecurityException("Bootstrap setup is already completed and permanently disabled.");
        }

        if (!isActivationKeyValid(activationKey)) {
            throw new SecurityException("Invalid administrator activation secret key.");
        }

        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }

        createAdministrator(username.trim(), password, email.trim(), fullName != null ? fullName.trim() : "Administrator");
        // Clear fallback secret from memory
        runtimeFallbackSecret = null;
    }

    private void createAdministrator(String username, String password, String email, String fullName) {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    role.setDescription("Full system administrator access");
                    return roleRepository.save(role);
                });

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setRole(adminRole);
        admin.setActive(true);
        admin.setMustChangePassword(false);
        admin.setTemporaryPassword(false);

        userRepository.save(admin);
        log.info("[AUDIT] Initial system administrator '{}' created successfully.", username);
    }

    private void ensureRoleExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            log.info("[ROLE] Initialized role: {}", roleName);
        }
    }
}
