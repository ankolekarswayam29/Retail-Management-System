package com.retailpro.retailmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Retail Management System
 * 
 * This is the entry point for the Spring Boot application.
 * The @SpringBootApplication annotation includes:
 * - @Configuration: Marks the class as a source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot's auto-configuration
 * - @ComponentScan: Enables component scanning for @Component, @Service, @Controller, etc.
 */
@SpringBootApplication
public class RetailManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailManagementSystemApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Retail Management System Started Successfully!");
        System.out.println("Access the application at: http://localhost:8080");
        System.out.println("=================================================");
    }
}
