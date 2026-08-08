package com.campusworks.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for validating user email addresses.
 * Accepts any standard email format (not restricted to college domains).
 */
@Service
public class EmailValidationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailValidationService.class);

    // Standard email pattern (local@domain)
    private static final Pattern DEFAULT_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Value("${app.email.pattern:^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$}")
    private String emailPattern;

    private Pattern pattern;

    private Pattern getPattern() {
        if (pattern == null) {
            try {
                pattern = Pattern.compile(emailPattern);
            } catch (Exception e) {
                logger.warn("Invalid app.email.pattern '{}', falling back to default", emailPattern);
                pattern = DEFAULT_EMAIL_PATTERN;
            }
            logger.info("Initialized email validation pattern: {}", emailPattern);
        }
        return pattern;
    }

    /**
     * Validate email format (any valid email address).
     * Kept method name for compatibility with existing AuthService callers.
     */
    public boolean isValidCollegeEmail(String email) {
        return isValidEmail(email);
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("Email validation failed: empty");
            return false;
        }

        String normalized = email.trim().toLowerCase();
        boolean isValid = getPattern().matcher(normalized).matches();

        if (isValid) {
            logger.debug("Email validation successful: {}", normalized);
        } else {
            logger.debug("Email validation failed: {}", normalized);
        }

        return isValid;
    }

    public String extractStudentId(String email) {
        return null;
    }

    public String extractStudentYear(String email) {
        return null;
    }

    public String getValidationErrorMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email address is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address (example: you@gmail.com)";
        }
        return "Invalid email format";
    }

    public String getCollegeDomain() {
        return "";
    }

    public String[] getExampleEmails() {
        return new String[]{
            "you@gmail.com",
            "student@outlook.com",
            "user@example.com"
        };
    }
}
