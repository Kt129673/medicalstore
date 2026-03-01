package com.medicalstore.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * RoleBasedAccessConfiguration — registers the role-based access interceptor.
 * 
 * This enables automatic logging of access attempts across the application.
 */
@Configuration
@RequiredArgsConstructor
public class RoleBasedAccessConfiguration implements WebMvcConfigurer {

    private final RoleBasedAccessInterceptor roleBasedAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleBasedAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/error", "/css/**", "/js/**", "/images/**");
    }
}
