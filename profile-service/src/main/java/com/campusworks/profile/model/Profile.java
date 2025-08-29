package com.campusworks.profile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Profile Entity
 * Represents a user's profile information and preferences
 */
@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @NotBlank(message = "User email is required")
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    @Column(length = 1000)
    private String bio;
    
    @Size(max = 100, message = "University cannot exceed 100 characters")
    @Column(length = 100)
    private String university;
    
    @Size(max = 100, message = "Major cannot exceed 100 characters")
    @Column(length = 100)
    private String major;
    
    @Min(value = 1, message = "Year must be at least 1")
    @Max(value = 10, message = "Year cannot exceed 10")
    @Column(name = "academic_year")
    private Integer academicYear;
    
    @ElementCollection
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill", length = 100)
    @Size(max = 20, message = "Cannot have more than 20 skills")
    private List<String> skills;
    
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Size(max = 500, message = "Experience description cannot exceed 500 characters")
    @Column(name = "experience_description", length = 500)
    private String experienceDescription;
    
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    
    @Column(name = "total_ratings")
    @Min(value = 0, message = "Total ratings cannot be negative")
    private Integer totalRatings;
    
    @Column(name = "completed_tasks")
    @Min(value = 0, message = "Completed tasks cannot be negative")
    private Integer completedTasks;
    
    @Column(name = "successful_tasks")
    @Min(value = 0, message = "Successful tasks cannot be negative")
    private Integer successfulTasks;
    
    @Column(name = "total_earnings", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Total earnings cannot be negative")
    private BigDecimal totalEarnings;
    
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;
    
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;
    
    @ElementCollection
    @CollectionTable(name = "profile_preferred_categories", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "category", length = 100)
    private List<String> preferredCategories;
    
    @Column(name = "hourly_rate", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;
    
    @Column(name = "availability_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;
    
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Business Logic Methods
    
    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return userEmail;
        }
    }
    
    /**
     * Get display name
     */
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName.charAt(0) + ".";
        } else if (firstName != null) {
            return firstName;
        } else {
            return userEmail.split("@")[0];
        }
    }
    
    /**
     * Check if profile is complete
     */
    public boolean isComplete() {
        return firstName != null && lastName != null && bio != null && 
               university != null && major != null && academicYear != null &&
               skills != null && !skills.isEmpty();
    }
    
    /**
     * Check if user is available for work
     */
    public boolean isAvailableForWork() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE && 
               isPublic && isVerified;
    }
    
    /**
     * Add a new rating
     */
    public void addRating(BigDecimal newRating) {
        if (rating == null) {
            rating = newRating;
            totalRatings = 1;
        } else {
            BigDecimal totalRating = rating.multiply(BigDecimal.valueOf(totalRatings));
            totalRating = totalRating.add(newRating);
            totalRatings++;
            rating = totalRating.divide(BigDecimal.valueOf(totalRatings), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * Update rating when task is completed
     */
    public void updateRating(BigDecimal newRating) {
        addRating(newRating);
        if (completedTasks == null) {
            completedTasks = 1;
        } else {
            completedTasks++;
        }
    }
    
    /**
     * Mark task as successful
     */
    public void markTaskSuccessful() {
        if (successfulTasks == null) {
            successfulTasks = 1;
        } else {
            successfulTasks++;
        }
    }
    
    /**
     * Add earnings
     */
    public void addEarnings(BigDecimal amount) {
        if (totalEarnings == null) {
            totalEarnings = amount;
        } else {
            totalEarnings = totalEarnings.add(amount);
        }
    }
    
    /**
     * Update last active time
     */
    public void updateLastActive() {
        this.lastActive = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set availability status
     */
    public void setAvailabilityStatus(AvailabilityStatus status) {
        this.availabilityStatus = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verify profile
     */
    public void verifyProfile() {
        this.isVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Set profile visibility
     */
    public void setProfileVisibility(boolean isPublic) {
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Enums
    
    /**
     * Availability Status
     */
    public enum AvailabilityStatus {
        AVAILABLE("Available for work"),
        BUSY("Currently busy"),
        UNAVAILABLE("Not available"),
        ON_BREAK("On break");
        
        private final String displayName;
        
        AvailabilityStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
