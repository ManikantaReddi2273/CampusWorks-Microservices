package com.campusworks.auth.service;

import com.campusworks.auth.model.User;
import com.campusworks.auth.repo.UserRepository;
import com.campusworks.auth.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Auth Service
 * Handles user registration, authentication, and JWT token generation
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Register a new user
     * All new registrations default to STUDENT role
     * @param email user's email address
     * @param password user's password
     * @return created user object
     */
    public User register(String email, String password) {
        logger.info("📝 Attempting to register user with email: {}", email);
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            logger.warn("❌ User registration failed: Email {} already exists", email);
            throw new RuntimeException("User with this email already exists");
        }
        
        // Create new user with STUDENT role by default
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(User.UserRole.STUDENT) // Default to STUDENT role
                .enabled(true)
                .build();
        
        User savedUser = userRepository.save(user);
        logger.info("✅ User registered successfully: {} with ID: {} and role: {}", 
                   email, savedUser.getId(), savedUser.getRole());
        
        return savedUser;
    }
    
    /**
     * Authenticate user and generate JWT token
     * @param email user's email address
     * @param password user's password
     * @return JWT token string
     */
    public String login(String email, String password) {
        logger.info("🔐 Attempting login for user: {}", email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("❌ Login failed: User {} not found", email);
            throw new RuntimeException("Invalid credentials");
        }
        
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("❌ Login failed: Invalid password for user {}", email);
            throw new RuntimeException("Invalid credentials");
        }
        
        if (!user.isEnabled()) {
            logger.warn("❌ Login failed: User {} is disabled", email);
            throw new RuntimeException("Account is disabled");
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(
            user.getId().toString(), 
            user.getEmail(), 
            user.getRole().name()
        );
        
        logger.info("✅ User {} logged in successfully", email);
        return token;
    }
    
    /**
     * Find user by email
     * @param email user's email address
     * @return Optional containing user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by ID
     * @param id user's ID
     * @return Optional containing user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
