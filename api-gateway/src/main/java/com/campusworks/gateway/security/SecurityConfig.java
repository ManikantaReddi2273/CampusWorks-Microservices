package com.campusworks.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Simple CORS Configuration for API Gateway
 * Like Express.js app.use(cors()) - enables CORS for all routes
 */
@Configuration
public class SecurityConfig {
    
    /**
     * Simple CORS Filter - Enable CORS for all endpoints
     * Equivalent to Express.js: app.use(cors())
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow frontend origin
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
        
        // Allow all HTTP methods
        corsConfig.setAllowedMethods(List.of("*"));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
