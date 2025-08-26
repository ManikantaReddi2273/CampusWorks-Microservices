package com.campusworks.auth.controller;

import com.campusworks.auth.model.User;
import com.campusworks.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Auth Controller
 * Handles HTTP requests for user authentication and registration
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register a new user
     * All new registrations default to STUDENT role
     * @param request registration request containing email and password
     * @return response with user details
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("📝 Registration request received for email: {}", request.getEmail());
        
        try {
            // Role is automatically set to STUDENT in AuthService
            User user = authService.register(request.getEmail(), request.getPassword());
            
            logger.info("✅ User registered successfully: {}", user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully as STUDENT");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("❌ Registration failed for email: {} - Error: {}", 
                        request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user and return JWT token
     * @param request login request containing email and password
     * @return response with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("🔐 Login request received for email: {}", request.getEmail());
        
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            
            logger.info("✅ User logged in successfully: {}", request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("email", request.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Login failed for email: {} - Error: {}", 
                        request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint
     * @return service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("🏥 Health check endpoint called");
        return ResponseEntity.ok("Auth Service is running - Phase 1 ✅");
    }
    
    /**
     * Get user information by email
     * @param email user's email address
     * @return user details
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        logger.info("👤 Getting user information for email: {}", email);
        
        try {
            var userOpt = authService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("userId", user.getId());
                response.put("email", user.getEmail());
                response.put("role", user.getRole());
                response.put("enabled", user.isEnabled());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("❌ Error getting user by email: {} - Error: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving user: " + e.getMessage());
        }
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Registration request DTO
     */
    public static class RegisterRequest {
        private String email;
        private String password;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String email;
        private String password;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
