package com.retailpro.retailmanagementsystem.config;

import com.retailpro.retailmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * Initializes default data (roles and admin user) when the application starts
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("Initializing Default Data...");
        System.out.println("=================================================");

        // Initialize default roles
        userService.initializeDefaultRoles();
        System.out.println("✓ Default roles initialized (ADMIN, STAFF)");

        // Initialize default admin user
        userService.initializeDefaultAdmin();
        System.out.println("✓ Default admin user initialized");

        System.out.println("=================================================");
        System.out.println("Default Credentials:");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
        System.out.println("=================================================");
        System.out.println("⚠️  Please change the default password after first login!");
        System.out.println("=================================================");
    }
}
