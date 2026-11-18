package com.retailpro.retailmanagementsystem.config;

import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.repository.UserRepository;
import com.retailpro.retailmanagementsystem.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, UserRepository userRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers(
                                "/", "/login", "/setup", "/css/**", "/js/**",
                                "/images/**", "/error", "/static/**", "/webjars/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Password change (accessible to any authenticated user)
                        .requestMatchers("/change-password").authenticated()

                        // Admin-only management endpoints
                        .requestMatchers(
                                "/dashboard/admin", "/admin/**", "/users/**", "/categories/**",
                                "/suppliers/**", "/reports/**", "/products/new", "/products/edit/**",
                                "/products/save", "/products/delete/**", "/inventory/adjust/**"
                        ).hasRole("ADMIN")

                        // User dashboard (accessible to USER and ADMIN)
                        .requestMatchers("/dashboard/user", "/user/**").hasAnyRole("USER", "ADMIN")

                        // Shared retail operations
                        .requestMatchers(
                                "/dashboard", "/api/dashboard/**", "/products", "/products/view/**",
                                "/api/products/**", "/customers/**", "/billing/**", "/sales/**",
                                "/inventory", "/inventory/low-stock", "/inventory/transactions",
                                "/inventory/history/**"
                        ).hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();

                            // Record login timestamp
                            userRepository.findByUsername(username).ifPresent(user -> {
                                user.setLastLoginAt(LocalDateTime.now());
                                userRepository.save(user);
                            });

                            // Check if mandatory first-time password change is required
                            User user = userRepository.findByUsername(username).orElse(null);
                            if (user != null && Boolean.TRUE.equals(user.getMustChangePassword())) {
                                response.sendRedirect(request.getContextPath() + "/change-password");
                                return;
                            }

                            // Strictly determine destination from authenticated database authorities
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect(request.getContextPath() + "/dashboard/admin");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/dashboard/user");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof DisabledException) {
                                response.sendRedirect(request.getContextPath() + "/login?disabled=true");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login?error=true");
                            }
                        })
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .key("retailpro-secure-key-remember-me-2026")
                        .tokenValiditySeconds(86400 * 7) // 7 days
                        .userDetailsService(customUserDetailsService)
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect(request.getContextPath() + "/dashboard");
                        })
                )

                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
