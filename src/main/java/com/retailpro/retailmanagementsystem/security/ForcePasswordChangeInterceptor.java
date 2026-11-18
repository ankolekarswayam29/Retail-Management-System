package com.retailpro.retailmanagementsystem.security;

import com.retailpro.retailmanagementsystem.entity.User;
import com.retailpro.retailmanagementsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ForcePasswordChangeInterceptor
 * Intercepts requests for authenticated users whose mustChangePassword flag is true.
 * Redirects them strictly to /change-password until their temporary password is reset.
 */
@Component
public class ForcePasswordChangeInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Whitelist static resources and authentication / change-password endpoints
        if (uri.startsWith("/change-password") ||
            uri.startsWith("/logout") ||
            uri.startsWith("/login") ||
            uri.startsWith("/setup") ||
            uri.startsWith("/css/") ||
            uri.startsWith("/js/") ||
            uri.startsWith("/images/") ||
            uri.startsWith("/error") ||
            uri.startsWith("/static/")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);

            if (user != null && Boolean.TRUE.equals(user.getMustChangePassword())) {
                response.sendRedirect(request.getContextPath() + "/change-password");
                return false;
            }
        }

        return true;
    }
}
