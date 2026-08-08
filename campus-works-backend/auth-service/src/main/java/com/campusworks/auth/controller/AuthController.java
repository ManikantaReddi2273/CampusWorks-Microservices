package com.campusworks.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campusworks.auth.model.User;
import com.campusworks.auth.service.AuthService;

/**
 * Auth Controller
 * Handles HTTP requests for user authentication and registration
 */
@RestController
@RequestMapping("/auth")
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
            response.put("message", "Registration successful! Please check your email to verify your account.");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("emailVerificationRequired", true);
            response.put("emailVerified", user.isEmailVerified());
            response.put("accountEnabled", user.isEnabled());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("❌ Registration failed for email: {} - Error: {}", 
                        request.getEmail(), e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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
                response.put("emailVerified", user.isEmailVerified());
                response.put("emailVerifiedAt", user.getEmailVerifiedAt());
                response.put("createdAt", user.getCreatedAt());
                
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
    
    /**
     * Logout user by invalidating their JWT token
     * @param request HTTP request to extract Authorization header
     * @return logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        logger.info("🚪 Logout request received");
        
        try {
            // Extract token from Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warn("❌ Logout failed: Invalid Authorization header");
                return ResponseEntity.badRequest().body("Invalid Authorization header");
            }
            
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            
            // Perform logout
            boolean logoutSuccessful = authService.logout(token);
            
            if (logoutSuccessful) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Logout successful");
                response.put("timestamp", System.currentTimeMillis());
                
                logger.info("✅ User logged out successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.error("❌ Logout failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Logout failed");
            }
            
        } catch (Exception e) {
            logger.error("❌ Logout failed with error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate if a token is still valid (not blacklisted)
     * @param request HTTP request to extract Authorization header
     * @return token validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        logger.info("🔍 Token validation request received");
        
        try {
            // Extract token from Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Invalid Authorization header");
            }
            
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            
            // Validate token
            boolean isValid = authService.isTokenValid(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("timestamp", System.currentTimeMillis());
            
            if (isValid) {
                response.put("message", "Token is valid");
            } else {
                response.put("message", "Token is invalid or blacklisted");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Token validation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token validation failed");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Verify user email using verification token
     * @param token verification token from email link
     * @return verification result
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        logger.info("📧 Email verification request received for token: {}", token);
        
        try {
            boolean verificationSuccessful = authService.verifyEmail(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (verificationSuccessful) {
                response.put("success", true);
                response.put("message", "Email verified successfully! Your account is now active.");
                response.put("title", "Email Verification Successful");
                response.put("redirectUrl", "/login");
                
                logger.info("✅ Email verification successful for token: {}", token);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Email verification failed. The link may be invalid or expired.");
                response.put("title", "Email Verification Failed");
                response.put("canResend", true);
                
                logger.warn("❌ Email verification failed for token: {}", token);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Email verification error for token {}: {}", token, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred during email verification. Please try again.");
            response.put("title", "Verification Error");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Resend verification email (authenticated user)
     * @return resend result
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail() {
        logger.info("🔄 Resend verification email request");
        
        try {
            // Get current user from security context
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            if (currentUserEmail == null || currentUserEmail.equals("anonymousUser")) {
                logger.warn("❌ Resend verification failed: User not properly authenticated");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not properly authenticated");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            boolean emailSent = authService.resendVerificationEmail(currentUserEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (emailSent) {
                response.put("success", true);
                response.put("message", "Verification email sent successfully! Please check your inbox.");
                response.put("email", currentUserEmail);
                
                logger.info("✅ Verification email resent successfully to: {}", currentUserEmail);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send verification email. Please try again later.");
                
                logger.error("❌ Failed to resend verification email to: {}", currentUserEmail);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Resend verification email failed: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Resend verification email (public endpoint for unauthenticated users)
     * @param request resend verification request with email
     * @return resend result
     */
    @PostMapping("/resend-verification-public")
    public ResponseEntity<?> resendVerificationEmailPublic(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("🔄 Public resend verification email request for: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email address is required");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean emailSent = authService.resendVerificationEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (emailSent) {
                response.put("success", true);
                response.put("message", "Verification email sent successfully! Please check your inbox.");
                response.put("email", email);
                
                logger.info("✅ Public verification email resent successfully to: {}", email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send verification email. Please check your email address and try again.");
                
                logger.error("❌ Failed to resend verification email to: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Public resend verification email failed for {}: {}", email, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get verification status for an email
     * @param email email address to check
     * @return verification status
     */
    @GetMapping("/verification-status/{email}")
    public ResponseEntity<?> getVerificationStatus(@PathVariable String email) {
        logger.info("🔍 Verification status request for: {}", email);
        
        try {
            AuthService.VerificationStatus status = authService.getVerificationStatus(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("userExists", status.userExists);
            response.put("emailVerified", status.emailVerified);
            response.put("hasPendingVerificationToken", status.hasPendingVerificationToken);
            response.put("verifiedAt", status.verifiedAt);
            response.put("timestamp", System.currentTimeMillis());
            
            if (!status.userExists) {
                response.put("message", "User not found with this email address");
                return ResponseEntity.notFound().build();
            } else if (status.emailVerified) {
                response.put("message", "Email is already verified");
            } else if (status.hasPendingVerificationToken) {
                response.put("message", "Email verification pending - check your inbox");
            } else {
                response.put("message", "No pending verification token - you can request a new one");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error getting verification status for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving verification status: " + e.getMessage());
        }
    }
    
    /**
     * Forgot password - send reset email
     * @param request forgot password request containing email
     * @return reset email result
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        logger.info("🔑 Forgot password request for: {}", request.email);
        
        try {
            boolean emailSent = authService.forgotPassword(request.email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (emailSent) {
                response.put("success", true);
                response.put("message", "Password reset email sent successfully. Check your inbox.");
                response.put("title", "Reset Email Sent");
                
                logger.info("✅ Password reset email sent successfully to: {}", request.email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send reset email. Please try again.");
                response.put("title", "Email Send Failed");
                
                logger.warn("❌ Failed to send reset email to: {}", request.email);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Forgot password error for {}: {}", request.email, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while sending reset email. Please try again.");
            response.put("title", "Email Error");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Reset password with token
     * @param request reset password request containing token, email, and new password
     * @return reset result
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("🔑 Password reset request for: {}", request.email);
        
        try {
            // Validate request
            if (request.token == null || request.token.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Reset token is required.");
                response.put("title", "Validation Error");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.email == null || request.email.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Email is required.");
                response.put("title", "Validation Error");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.newPassword == null || request.newPassword.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "New password is required.");
                response.put("title", "Validation Error");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean resetSuccessful = authService.resetPassword(request.token, request.email, request.newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (resetSuccessful) {
                response.put("success", true);
                response.put("message", "Password reset successfully. You can now login with your new password.");
                response.put("title", "Password Reset");
                
                logger.info("✅ Password reset successfully for: {}", request.email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to reset password. Please try again.");
                response.put("title", "Reset Failed");
                
                logger.warn("❌ Password reset failed for: {}", request.email);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Password reset error for {}: {}", request.email, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while resetting password. Please try again.");
            response.put("title", "Reset Error");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Test endpoint to check authentication status
     * @return current user info
     */
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        logger.info("🔍 Testing authentication status");
        
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("authenticated", !currentUserEmail.equals("anonymousUser"));
            response.put("userEmail", currentUserEmail);
            response.put("authenticationType", SecurityContextHolder.getContext().getAuthentication().getClass().getSimpleName());
            
            logger.info("✅ Auth test completed. User: {}, Authenticated: {}", currentUserEmail, !currentUserEmail.equals("anonymousUser"));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Auth test error: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            response.put("authenticated", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Change password (authenticated user)
     * @param request change password request containing current and new password
     * @return change result
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        logger.info("🔑 Change password request");
        
        try {
            // Validate request
            if (request.currentPassword == null || request.currentPassword.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Current password is required.");
                response.put("title", "Validation Error");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.newPassword == null || request.newPassword.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "New password is required.");
                response.put("title", "Validation Error");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean changeSuccessful = authService.changePassword(request.currentPassword, request.newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (changeSuccessful) {
                response.put("success", true);
                response.put("message", "Password changed successfully.");
                response.put("title", "Password Changed");
                
                logger.info("✅ Password changed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to change password. Please check your current password.");
                response.put("title", "Change Failed");
                
                logger.warn("❌ Password change failed");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Change password error: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while changing password. Please try again.");
            response.put("title", "Change Error");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete user account
     * @param request delete account request containing email confirmation
     * @return deletion result
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@RequestBody DeleteAccountRequest request) {
        logger.info("🗑️ Account deletion request for: {}", request.email);
        
        try {
            boolean deletionSuccessful = authService.deleteAccount(request.email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", System.currentTimeMillis());
            
            if (deletionSuccessful) {
                response.put("success", true);
                response.put("message", "Account deleted successfully");
                response.put("title", "Account Deleted");
                
                logger.info("✅ Account deleted successfully for: {}", request.email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete account. Please try again.");
                response.put("title", "Deletion Failed");
                
                logger.warn("❌ Account deletion failed for: {}", request.email);
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Account deletion error for {}: {}", request.email, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred during account deletion. Please try again.");
            response.put("title", "Deletion Error");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Validate email format (utility endpoint) — any standard email is accepted.
     */
    @GetMapping("/validate-email/{email}")
    public ResponseEntity<?> validateEmail(@PathVariable String email) {
        logger.info("📧 Email validation request for: {}", email);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("timestamp", System.currentTimeMillis());

            boolean validFormat = email != null
                    && email.matches("(?i)^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            
            response.put("valid", validFormat);
            
            if (validFormat) {
                response.put("message", "Email format is valid");
            } else {
                response.put("message", "Invalid email format. Use a normal email like you@gmail.com");
                response.put("examples", new String[]{
                    "you@gmail.com",
                    "student@outlook.com",
                    "user@example.com"
                });
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Email validation error for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating email: " + e.getMessage());
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
    

    
    /**
     * Delete account request DTO
     */
    public static class DeleteAccountRequest {
        private String email;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    /**
     * Forgot password request DTO
     */
    public static class ForgotPasswordRequest {
        private String email;
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    /**
     * Reset password request DTO
     */
    public static class ResetPasswordRequest {
        private String token;
        private String email;
        private String newPassword;
        
        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * Change password request DTO
     */
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        
        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
