package com.campusworks.payment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@Slf4j
public class FeignClientConfig {

    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        String userId = request.getHeader("X-User-Id");
                        String userEmail = request.getHeader("X-User-Email");
                        String userRoles = request.getHeader("X-User-Roles");

                        if (userId != null) {
                            template.header("X-User-Id", userId);
                            log.debug("🔐 Propagated X-User-Id: {} to Feign Client request", userId);
                        }
                        if (userEmail != null) {
                            template.header("X-User-Email", userEmail);
                            log.debug("🔐 Propagated X-User-Email: {} to Feign Client request", userEmail);
                        }
                        if (userRoles != null) {
                            template.header("X-User-Roles", userRoles);
                            log.debug("🔐 Propagated X-User-Roles: {} to Feign Client request", userRoles);
                        }
                        log.info("💰 Payment Service Feign Client request headers propagated for user: {} ({})", userEmail, userId);
                    } else {
                        log.warn("⚠️ No request context available for Payment Service Feign Client header propagation");
                    }
                } catch (Exception e) {
                    log.error("❌ Error propagating headers to Payment Service Feign Client request: {}", e.getMessage(), e);
                }
            }
        };
    }
}
