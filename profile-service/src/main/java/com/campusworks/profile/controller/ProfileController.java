package com.campusworks.profile.controller;

import com.campusworks.profile.model.Profile;
import com.campusworks.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profile Controller
 * Handles HTTP requests for profile management
 */
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProfileController {
    
    private final ProfileService profileService;
    
    /**
     * Create a new profile
     */
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest request, HttpServletRequest httpRequest) {
        String userEmail = httpRequest.getHeader("X-User-Email");
        String userId = httpRequest.getHeader("X-User-Id");
        
        log.info("📝 Creating new profile for user: {} ({})", userEmail, userId);
        
        try {
            // Build profile from request
            Profile profile = Profile.builder()
                    .userId(Long.parseLong(userId))
                    .userEmail(userEmail)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .bio(request.getBio())
                    .university(request.getUniversity())
                    .major(request.getMajor())
                    .academicYear(request.getAcademicYear())
                    .skills(request.getSkills())
                    .experienceYears(request.getExperienceYears())
                    .experienceDescription(request.getExperienceDescription())
                    .preferredCategories(request.getPreferredCategories())
                    .hourlyRate(request.getHourlyRate())
                    .availabilityStatus(request.getAvailabilityStatus())
                    .isPublic(request.getIsPublic())
                    .build();
            
            // Create profile
            Profile createdProfile = profileService.createProfile(profile);
            
            log.info("✅ Profile created successfully for user: {} (ID: {})", createdProfile.getUserEmail(), createdProfile.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile created successfully");
            response.put("profileId", createdProfile.getId());
            response.put("displayName", createdProfile.getDisplayName());
            response.put("isComplete", createdProfile.isComplete());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to create profile for user: {} - Error: {}", userEmail, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get all profiles
     */
    @GetMapping
    public ResponseEntity<?> getAllProfiles() {
        log.info("📋 Retrieving all public profiles");
        
        try {
            List<Profile> profiles = profileService.getAllPublicProfiles();
            
            log.info("✅ Retrieved {} public profiles successfully", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profile by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        log.info("🔍 Retrieving profile with ID: {}", id);
        
        try {
            var profileOpt = profileService.getProfileById(id);
            
            if (profileOpt.isPresent()) {
                Profile profile = profileOpt.get();
                log.info("✅ Profile retrieved successfully: {} (ID: {})", profile.getDisplayName(), profile.getId());
                return ResponseEntity.ok(profile);
            } else {
                log.warn("❌ Profile not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profile by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        log.info("👤 Retrieving profile for user ID: {}", userId);
        
        try {
            var profileOpt = profileService.getProfileByUserId(userId);
            
            if (profileOpt.isPresent()) {
                Profile profile = profileOpt.get();
                log.info("✅ Profile retrieved successfully for user ID: {} - {}", userId, profile.getDisplayName());
                return ResponseEntity.ok(profile);
            } else {
                log.info("ℹ️ No profile found for user ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profile for user ID: {} - Error: {}", userId, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profile by user email
     */
    @GetMapping("/user/email/{userEmail}")
    public ResponseEntity<?> getProfileByUserEmail(@PathVariable String userEmail) {
        log.info("📧 Retrieving profile for user email: {}", userEmail);
        
        try {
            var profileOpt = profileService.getProfileByUserEmail(userEmail);
            
            if (profileOpt.isPresent()) {
                Profile profile = profileOpt.get();
                log.info("✅ Profile retrieved successfully for user email: {} - {}", userEmail, profile.getDisplayName());
                return ResponseEntity.ok(profile);
            } else {
                log.info("ℹ️ No profile found for user email: {}", userEmail);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profile for user email: {} - Error: {}", userEmail, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all verified profiles
     */
    @GetMapping("/verified")
    public ResponseEntity<?> getVerifiedProfiles() {
        log.info("✅ Retrieving all verified profiles");
        
        try {
            List<Profile> profiles = profileService.getAllVerifiedProfiles();
            
            log.info("✅ Retrieved {} verified profiles successfully", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve verified profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve verified profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by availability status
     */
    @GetMapping("/availability/{status}")
    public ResponseEntity<?> getProfilesByAvailabilityStatus(@PathVariable String status) {
        log.info("🏷️ Retrieving profiles with availability status: {}", status);
        
        try {
            Profile.AvailabilityStatus availabilityStatus = Profile.AvailabilityStatus.valueOf(status.toUpperCase());
            List<Profile> profiles = profileService.getProfilesByAvailabilityStatus(availabilityStatus);
            
            log.info("✅ Retrieved {} profiles with availability status: {}", profiles.size(), status);
            
            return ResponseEntity.ok(profiles);
            
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid availability status: {}", status);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid availability status");
            errorResponse.put("message", "Status must be one of: AVAILABLE, BUSY, UNAVAILABLE, ON_BREAK");
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles with availability status: {} - Error: {}", status, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by university
     */
    @GetMapping("/university/{university}")
    public ResponseEntity<?> getProfilesByUniversity(@PathVariable String university) {
        log.info("🏫 Retrieving profiles for university: {}", university);
        
        try {
            List<Profile> profiles = profileService.getProfilesByUniversity(university);
            
            log.info("✅ Retrieved {} profiles for university: {}", profiles.size(), university);
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles for university: {} - Error: {}", university, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by major
     */
    @GetMapping("/major/{major}")
    public ResponseEntity<?> getProfilesByMajor(@PathVariable String major) {
        log.info("📚 Retrieving profiles for major: {}", major);
        
        try {
            List<Profile> profiles = profileService.getProfilesByMajor(major);
            
            log.info("✅ Retrieved {} profiles for major: {}", profiles.size(), major);
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles for major: {} - Error: {}", major, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by skill
     */
    @GetMapping("/skill/{skill}")
    public ResponseEntity<?> getProfilesBySkill(@PathVariable String skill) {
        log.info("🛠️ Retrieving profiles with skill: {}", skill);
        
        try {
            List<Profile> profiles = profileService.getProfilesBySkill(skill);
            
            log.info("✅ Retrieved {} profiles with skill: {}", profiles.size(), skill);
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles with skill: {} - Error: {}", skill, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by rating range
     */
    @GetMapping("/rating")
    public ResponseEntity<?> getProfilesByRatingRange(
            @RequestParam BigDecimal minRating,
            @RequestParam BigDecimal maxRating) {
        log.info("⭐ Retrieving profiles with rating between {} and {}", minRating, maxRating);
        
        try {
            List<Profile> profiles = profileService.getProfilesByRatingRange(minRating, maxRating);
            
            log.info("✅ Retrieved {} profiles with rating between {} and {}", profiles.size(), minRating, maxRating);
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles by rating range - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles by hourly rate range
     */
    @GetMapping("/hourly-rate")
    public ResponseEntity<?> getProfilesByHourlyRateRange(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate) {
        log.info("💰 Retrieving profiles with hourly rate between ${} and ${}", minRate, maxRate);
        
        try {
            List<Profile> profiles = profileService.getProfilesByHourlyRateRange(minRate, maxRate);
            
            log.info("✅ Retrieved {} profiles with hourly rate between ${} and ${}", profiles.size(), minRate, maxRate);
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles by hourly rate range - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get profiles available for work
     */
    @GetMapping("/available-for-work")
    public ResponseEntity<?> getProfilesAvailableForWork() {
        log.info("👷 Retrieving profiles available for work");
        
        try {
            List<Profile> profiles = profileService.getProfilesAvailableForWork();
            
            log.info("✅ Retrieved {} profiles available for work", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profiles available for work - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get top rated profiles
     */
    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedProfiles() {
        log.info("🏆 Retrieving top rated profiles");
        
        try {
            List<Profile> profiles = profileService.getTopRatedProfiles();
            
            log.info("✅ Retrieved {} top rated profiles", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve top rated profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get most experienced profiles
     */
    @GetMapping("/most-experienced")
    public ResponseEntity<?> getMostExperiencedProfiles() {
        log.info("🎓 Retrieving most experienced profiles");
        
        try {
            List<Profile> profiles = profileService.getMostExperiencedProfiles();
            
            log.info("✅ Retrieved {} most experienced profiles", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve most experienced profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get most successful profiles
     */
    @GetMapping("/most-successful")
    public ResponseEntity<?> getMostSuccessfulProfiles() {
        log.info("✅ Retrieving most successful profiles");
        
        try {
            List<Profile> profiles = profileService.getMostSuccessfulProfiles();
            
            log.info("✅ Retrieved {} most successful profiles", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve most successful profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get highest earning profiles
     */
    @GetMapping("/highest-earning")
    public ResponseEntity<?> getHighestEarningProfiles() {
        log.info("💎 Retrieving highest earning profiles");
        
        try {
            List<Profile> profiles = profileService.getHighestEarningProfiles();
            
            log.info("✅ Retrieved {} highest earning profiles", profiles.size());
            
            return ResponseEntity.ok(profiles);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve highest earning profiles - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profiles");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        String userEmail = httpRequest.getHeader("X-User-Email");
        String userId = httpRequest.getHeader("X-User-Id");
        
        log.info("✏️ Updating profile ID: {} by user: {} ({})", id, userEmail, userId);
        
        try {
            // Build updated profile
            Profile updatedProfile = Profile.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .bio(request.getBio())
                    .university(request.getUniversity())
                    .major(request.getMajor())
                    .academicYear(request.getAcademicYear())
                    .skills(request.getSkills())
                    .experienceYears(request.getExperienceYears())
                    .experienceDescription(request.getExperienceDescription())
                    .preferredCategories(request.getPreferredCategories())
                    .hourlyRate(request.getHourlyRate())
                    .availabilityStatus(request.getAvailabilityStatus())
                    .isPublic(request.getIsPublic())
                    .build();
            
            // Update profile
            Profile savedProfile = profileService.updateProfile(id, updatedProfile, Long.parseLong(userId));
            
            log.info("✅ Profile updated successfully: {} (ID: {})", savedProfile.getDisplayName(), savedProfile.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("profileId", savedProfile.getId());
            response.put("displayName", savedProfile.getDisplayName());
            response.put("isComplete", savedProfile.isComplete());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to update profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Delete profile
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long id, HttpServletRequest httpRequest) {
        String userEmail = httpRequest.getHeader("X-User-Email");
        String userId = httpRequest.getHeader("X-User-Id");
        
        log.info("🗑️ Deleting profile ID: {} by user: {} ({})", id, userEmail, userId);
        
        try {
            profileService.deleteProfile(id, Long.parseLong(userId));
            
            log.info("✅ Profile deleted successfully (ID: {})", id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to delete profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Add rating to profile
     */
    @PostMapping("/{id}/rating")
    public ResponseEntity<?> addRating(@PathVariable Long id, @RequestBody AddRatingRequest request) {
        log.info("⭐ Adding rating {} to profile ID: {}", request.getRating(), id);
        
        try {
            Profile updatedProfile = profileService.addRating(id, request.getRating());
            
            log.info("✅ Rating added successfully: {} to profile: {} (New rating: {})", 
                    request.getRating(), updatedProfile.getDisplayName(), updatedProfile.getRating());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rating added successfully");
            response.put("profileId", updatedProfile.getId());
            response.put("newRating", updatedProfile.getRating());
            response.put("totalRatings", updatedProfile.getTotalRatings());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to add rating to profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add rating");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Mark task as completed
     */
    @PostMapping("/{id}/task-completed")
    public ResponseEntity<?> markTaskCompleted(@PathVariable Long id) {
        log.info("✅ Marking task as completed for profile ID: {}", id);
        
        try {
            Profile updatedProfile = profileService.markTaskCompleted(id);
            
            log.info("✅ Task marked as completed for profile: {} (Total completed: {})", 
                    updatedProfile.getDisplayName(), updatedProfile.getCompletedTasks());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task marked as completed");
            response.put("profileId", updatedProfile.getId());
            response.put("totalCompletedTasks", updatedProfile.getCompletedTasks());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to mark task as completed for profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to mark task as completed");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Mark task as successful
     */
    @PostMapping("/{id}/task-successful")
    public ResponseEntity<?> markTaskSuccessful(@PathVariable Long id) {
        log.info("🎉 Marking task as successful for profile ID: {}", id);
        
        try {
            Profile updatedProfile = profileService.markTaskSuccessful(id);
            
            log.info("✅ Task marked as successful for profile: {} (Total successful: {})", 
                    updatedProfile.getDisplayName(), updatedProfile.getSuccessfulTasks());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task marked as successful");
            response.put("profileId", updatedProfile.getId());
            response.put("totalSuccessfulTasks", updatedProfile.getSuccessfulTasks());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to mark task as successful for profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to mark task as successful");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Add earnings to profile
     */
    @PostMapping("/{id}/earnings")
    public ResponseEntity<?> addEarnings(@PathVariable Long id, @RequestBody AddEarningsRequest request) {
        log.info("💰 Adding earnings ${} to profile ID: {}", request.getAmount(), id);
        
        try {
            Profile updatedProfile = profileService.addEarnings(id, request.getAmount());
            
            log.info("✅ Earnings added successfully: ${} to profile: {} (Total earnings: ${})", 
                    request.getAmount(), updatedProfile.getDisplayName(), updatedProfile.getTotalEarnings());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Earnings added successfully");
            response.put("profileId", updatedProfile.getId());
            response.put("amountAdded", request.getAmount());
            response.put("totalEarnings", updatedProfile.getTotalEarnings());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to add earnings to profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add earnings");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Verify profile
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyProfile(@PathVariable Long id) {
        log.info("✅ Verifying profile ID: {}", id);
        
        try {
            Profile verifiedProfile = profileService.verifyProfile(id);
            
            log.info("✅ Profile verified successfully: {} (ID: {})", verifiedProfile.getDisplayName(), verifiedProfile.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile verified successfully");
            response.put("profileId", verifiedProfile.getId());
            response.put("displayName", verifiedProfile.getDisplayName());
            response.put("isVerified", verifiedProfile.getIsVerified());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to verify profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to verify profile");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Update availability status
     */
    @PostMapping("/{id}/availability")
    public ResponseEntity<?> updateAvailabilityStatus(@PathVariable Long id, @RequestBody UpdateAvailabilityRequest request, HttpServletRequest httpRequest) {
        String userEmail = httpRequest.getHeader("X-User-Email");
        String userId = httpRequest.getHeader("X-User-Id");
        
        log.info("🔄 Updating availability status to {} for profile ID: {} by user: {} ({})", 
                request.getStatus(), id, userEmail, userId);
        
        try {
            Profile updatedProfile = profileService.updateAvailabilityStatus(id, request.getStatus(), Long.parseLong(userId));
            
            log.info("✅ Availability status updated successfully: {} for profile: {} (ID: {})", 
                    request.getStatus(), updatedProfile.getDisplayName(), updatedProfile.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Availability status updated successfully");
            response.put("profileId", updatedProfile.getId());
            response.put("newStatus", updatedProfile.getAvailabilityStatus());
            response.put("displayName", updatedProfile.getDisplayName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to update availability status for profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update availability status");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Update last active time
     */
    @PostMapping("/{id}/last-active")
    public ResponseEntity<?> updateLastActive(@PathVariable Long id) {
        log.info("⏰ Updating last active time for profile ID: {}", id);
        
        try {
            Profile updatedProfile = profileService.updateLastActive(id);
            
            log.info("✅ Last active time updated for profile: {} (ID: {})", updatedProfile.getDisplayName(), updatedProfile.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Last active time updated successfully");
            response.put("profileId", updatedProfile.getId());
            response.put("lastActive", updatedProfile.getLastActive());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to update last active time for profile ID: {} - Error: {}", id, e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update last active time");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get profile statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getProfileStatistics() {
        log.info("📊 Retrieving profile statistics");
        
        try {
            var stats = profileService.getProfileStatistics();
            
            log.info("✅ Profile statistics retrieved successfully");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Failed to retrieve profile statistics - Error: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve profile statistics");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("🏥 Health check endpoint called");
        return ResponseEntity.ok("Profile Service is running - Phase 2 ✅");
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Create Profile Request DTO
     */
    @lombok.Data
    public static class CreateProfileRequest {
        private String firstName;
        private String lastName;
        private String bio;
        private String university;
        private String major;
        private Integer academicYear;
        private List<String> skills;
        private Integer experienceYears;
        private String experienceDescription;
        private List<String> preferredCategories;
        private BigDecimal hourlyRate;
        private Profile.AvailabilityStatus availabilityStatus;
        private Boolean isPublic;
    }
    
    /**
     * Update Profile Request DTO
     */
    @lombok.Data
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String bio;
        private String university;
        private String major;
        private Integer academicYear;
        private List<String> skills;
        private Integer experienceYears;
        private String experienceDescription;
        private List<String> preferredCategories;
        private BigDecimal hourlyRate;
        private Profile.AvailabilityStatus availabilityStatus;
        private Boolean isPublic;
    }
    
    /**
     * Add Rating Request DTO
     */
    @lombok.Data
    public static class AddRatingRequest {
        private BigDecimal rating;
    }
    
    /**
     * Add Earnings Request DTO
     */
    @lombok.Data
    public static class AddEarningsRequest {
        private BigDecimal amount;
    }
    
    /**
     * Update Availability Request DTO
     */
    @lombok.Data
    public static class UpdateAvailabilityRequest {
        private Profile.AvailabilityStatus status;
    }
}
