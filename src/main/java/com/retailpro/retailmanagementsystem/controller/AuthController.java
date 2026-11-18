
package com.retailpro.retailmanagementsystem.controller;

import com.retailpro.retailmanagementsystem.entity.LoginRequest;
import com.retailpro.retailmanagementsystem.entity.LoginResponse;
import com.retailpro.retailmanagementsystem.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager) {

        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {
            // Authenticate using Spring Security
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUsername(),
                                    request.getPassword()
                            )
                    );

            // Execute existing login logic
            LoginResponse response = authService.login(request);

            // Create authenticated security context
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Save authentication into HTTP session
            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            SecurityContextHolder.clearContext();

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Authentication failed"
                    ));
        }
    }
}
