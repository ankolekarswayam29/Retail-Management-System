package com.retailpro.retailmanagementsystem.config;

import com.retailpro.retailmanagementsystem.security.AdminBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * Initializes system roles and executes secure bootstrap if required.
 * Preserves all existing database records and prevents password resets.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private AdminBootstrapService adminBootstrapService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=================================================");
        log.info("RetailPro Enterprise Platform Startup Verification");
        log.info("=================================================");

        adminBootstrapService.initializeSystem();

        log.info("✓ System verification and role initialization complete.");
        log.info("=================================================");
    }
}
