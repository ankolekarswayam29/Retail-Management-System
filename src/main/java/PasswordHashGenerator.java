package com.retailpro.retailmanagementsystem;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "RetailAdmin@123";

        String hashedPassword =
                encoder.encode(password);

        System.out.println("Original password: " + password);

        System.out.println("BCrypt hash: " + hashedPassword);

        System.out.println(
                "Verification: " +
                        encoder.matches(password, hashedPassword)
        );
    }
}
