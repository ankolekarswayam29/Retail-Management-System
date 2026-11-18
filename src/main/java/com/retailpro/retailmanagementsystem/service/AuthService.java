
package com.retailpro.retailmanagementsystem.service;

import com.retailpro.retailmanagementsystem.entity.LoginRequest;
import com.retailpro.retailmanagementsystem.entity.LoginResponse;
import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Account is inactive");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponse(
                "Login Successful",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
