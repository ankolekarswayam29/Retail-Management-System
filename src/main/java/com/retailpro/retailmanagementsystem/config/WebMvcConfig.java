package com.retailpro.retailmanagementsystem.config;

import com.retailpro.retailmanagementsystem.security.ForcePasswordChangeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfig
 * Registers web interceptors such as mandatory password change verification.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ForcePasswordChangeInterceptor forcePasswordChangeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(forcePasswordChangeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/static/**", "/error");
    }
}
