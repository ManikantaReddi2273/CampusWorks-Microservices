package com.campusworks.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RazorpayConfig {
    
    @Value("${payment.razorpay.key-id}")
    private String keyId;
    
    @Value("${payment.razorpay.key-secret}")
    private String keySecret;
    
    @Value("${payment.razorpay.mode}")
    private String mode;
    
    @Bean
    public RazorpayClient razorpayClient() {
        try {
            log.info("🔑 Initializing Razorpay Client in {} mode", mode);
            log.info("🔑 Using Key ID: {}", keyId.substring(0, Math.min(keyId.length(), 12)) + "...");
            
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            log.info("✅ Razorpay Client initialized successfully");
            return client;
            
        } catch (RazorpayException e) {
            log.error("❌ Failed to initialize Razorpay Client: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Razorpay Client", e);
        }
    }
}
