package com.campusworks.profile.service;

import com.campusworks.profile.model.Profile;
import com.campusworks.profile.repo.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Profile Service
 * Handles business logic for profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    
    @Value("${profile.max-bio-length:1000}")
    private int maxBioLength;
    
    @Value("${profile.max-skills-count:20}")
    private int maxSkillsCount;
    
    @Value("${profile.max-experience-years:50}")
    private int maxExperienceYears;
    
    @Value("${profile.rating-scale:5}")
    private String ratingScale;
    
    /**
     * Create a new profile
     */
    public Profile createProfile(Profile profile) {
        log.info("📝 Creating new profile for user: {} ({})", profile.getUserEmail(), profile.getUserId());
        
        // Validate profile data
        validateProfileData(profile);
        
        // Check if profile already exists for this user
        if (profileRepository.existsByUserId(profile.getUserId())) {
            log.warn("❌ Profile already exists for user ID: {}", profile.getUserId());
            throw new RuntimeException("Profile already exists for this user");
        }
        
        // Set default values
        profile.setRating(BigDecimal.ZERO);
        profile.setTotalRatings(0);
        profile.setCompletedTasks(0);
        profile.setSuccessfulTasks(0);
        profile.setTotalEarnings(BigDecimal.ZERO);
        profile.setIsVerified(false);
        profile.setIsPublic(true);
        profile.setAvailabilityStatus(Profile.AvailabilityStatus.AVAILABLE);
        profile.setLastActive(LocalDateTime.now());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        // Save profile
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Profile created successfully for user: {} (ID: {})", savedProfile.getUserEmail(), savedProfile.getId());
        
        return savedProfile;
    }
    
    /**
     * Get profile by ID
     */
    public Optional<Profile> getProfileById(Long id) {
        log.info("🔍 Retrieving profile with ID: {}", id);
        
        Optional<Profile> profile = profileRepository.findById(id);
        
        if (profile.isPresent()) {
            log.info("✅ Profile found: {} (ID: {})", profile.get().getDisplayName(), profile.get().getId());
        } else {
            log.warn("❌ Profile not found with ID: {}", id);
        }
        
        return profile;
    }
    
    /**
     * Get profile by user ID
     */
    public Optional<Profile> getProfileByUserId(Long userId) {
        log.info("👤 Retrieving profile for user ID: {}", userId);
        
        Optional<Profile> profile = profileRepository.findByUserId(userId);
        
        if (profile.isPresent()) {
            log.info("✅ Profile found for user ID: {} - {}", userId, profile.get().getDisplayName());
        } else {
            log.info("ℹ️ No profile found for user ID: {}", userId);
        }
        
        return profile;
    }
    
    /**
     * Get profile by user email
     */
    public Optional<Profile> getProfileByUserEmail(String userEmail) {
        log.info("📧 Retrieving profile for user email: {}", userEmail);
        
        Optional<Profile> profile = profileRepository.findByUserEmail(userEmail);
        
        if (profile.isPresent()) {
            log.info("✅ Profile found for user email: {} - {}", userEmail, profile.get().getDisplayName());
        } else {
            log.info("ℹ️ No profile found for user email: {}", userEmail);
        }
        
        return profile;
    }
    
    /**
     * Get all public profiles
     */
    public List<Profile> getAllPublicProfiles() {
        log.info("🌐 Retrieving all public profiles");
        
        List<Profile> profiles = profileRepository.findByIsPublicTrueOrderByRatingDesc();
        
        log.info("✅ Retrieved {} public profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get all verified profiles
     */
    public List<Profile> getAllVerifiedProfiles() {
        log.info("✅ Retrieving all verified profiles");
        
        List<Profile> profiles = profileRepository.findByIsVerifiedTrueOrderByRatingDesc();
        
        log.info("✅ Retrieved {} verified profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get profiles by availability status
     */
    public List<Profile> getProfilesByAvailabilityStatus(Profile.AvailabilityStatus status) {
        log.info("🏷️ Retrieving profiles with availability status: {}", status);
        
        List<Profile> profiles = profileRepository.findByAvailabilityStatusOrderByRatingDesc(status);
        
        log.info("✅ Retrieved {} profiles with availability status: {}", profiles.size(), status);
        
        return profiles;
    }
    
    /**
     * Get profiles by university
     */
    public List<Profile> getProfilesByUniversity(String university) {
        log.info("🏫 Retrieving profiles for university: {}", university);
        
        List<Profile> profiles = profileRepository.findByUniversityOrderByRatingDesc(university);
        
        log.info("✅ Retrieved {} profiles for university: {}", profiles.size(), university);
        
        return profiles;
    }
    
    /**
     * Get profiles by major
     */
    public List<Profile> getProfilesByMajor(String major) {
        log.info("📚 Retrieving profiles for major: {}", major);
        
        List<Profile> profiles = profileRepository.findByMajorOrderByRatingDesc(major);
        
        log.info("✅ Retrieved {} profiles for major: {}", profiles.size(), major);
        
        return profiles;
    }
    
    /**
     * Get profiles by skill
     */
    public List<Profile> getProfilesBySkill(String skill) {
        log.info("🛠️ Retrieving profiles with skill: {}", skill);
        
        List<Profile> profiles = profileRepository.findBySkill(skill);
        
        log.info("✅ Retrieved {} profiles with skill: {}", profiles.size(), skill);
        
        return profiles;
    }
    
    /**
     * Get profiles by rating range
     */
    public List<Profile> getProfilesByRatingRange(BigDecimal minRating, BigDecimal maxRating) {
        log.info("⭐ Retrieving profiles with rating between {} and {}", minRating, maxRating);
        
        List<Profile> profiles = profileRepository.findByRatingRange(minRating, maxRating);
        
        log.info("✅ Retrieved {} profiles with rating between {} and {}", profiles.size(), minRating, maxRating);
        
        return profiles;
    }
    
    /**
     * Get profiles by hourly rate range
     */
    public List<Profile> getProfilesByHourlyRateRange(BigDecimal minRate, BigDecimal maxRate) {
        log.info("💰 Retrieving profiles with hourly rate between ${} and ${}", minRate, maxRate);
        
        List<Profile> profiles = profileRepository.findByHourlyRateRange(minRate, maxRate);
        
        log.info("✅ Retrieved {} profiles with hourly rate between ${} and ${}", profiles.size(), minRate, maxRate);
        
        return profiles;
    }
    
    /**
     * Get profiles available for work
     */
    public List<Profile> getProfilesAvailableForWork() {
        log.info("👷 Retrieving profiles available for work");
        
        List<Profile> profiles = profileRepository.findAvailableForWork();
        
        log.info("✅ Retrieved {} profiles available for work", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get top rated profiles
     */
    public List<Profile> getTopRatedProfiles() {
        log.info("🏆 Retrieving top rated profiles");
        
        List<Profile> profiles = profileRepository.findTopRatedProfiles();
        
        log.info("✅ Retrieved {} top rated profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get most experienced profiles
     */
    public List<Profile> getMostExperiencedProfiles() {
        log.info("🎓 Retrieving most experienced profiles");
        
        List<Profile> profiles = profileRepository.findMostExperiencedProfiles();
        
        log.info("✅ Retrieved {} most experienced profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get most successful profiles
     */
    public List<Profile> getMostSuccessfulProfiles() {
        log.info("✅ Retrieving most successful profiles");
        
        List<Profile> profiles = profileRepository.findMostSuccessfulProfiles();
        
        log.info("✅ Retrieved {} most successful profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Get highest earning profiles
     */
    public List<Profile> getHighestEarningProfiles() {
        log.info("💎 Retrieving highest earning profiles");
        
        List<Profile> profiles = profileRepository.findHighestEarningProfiles();
        
        log.info("✅ Retrieved {} highest earning profiles", profiles.size());
        
        return profiles;
    }
    
    /**
     * Update profile
     */
    public Profile updateProfile(Long profileId, Profile updatedProfile, Long userId) {
        log.info("✏️ Updating profile ID: {} by user ID: {}", profileId, userId);
        
        // Check if profile exists and user owns it
        Optional<Profile> existingProfileOpt = profileRepository.findById(profileId);
        
        if (existingProfileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile existingProfile = existingProfileOpt.get();
        
        // Check if user owns this profile
        if (!existingProfile.getUserId().equals(userId)) {
            log.warn("❌ User {} is not authorized to update profile ID: {}", userId, profileId);
            throw new RuntimeException("You are not authorized to update this profile");
        }
        
        // Update allowed fields
        existingProfile.setFirstName(updatedProfile.getFirstName());
        existingProfile.setLastName(updatedProfile.getLastName());
        existingProfile.setBio(updatedProfile.getBio());
        existingProfile.setUniversity(updatedProfile.getUniversity());
        existingProfile.setMajor(updatedProfile.getMajor());
        existingProfile.setAcademicYear(updatedProfile.getAcademicYear());
        existingProfile.setSkills(updatedProfile.getSkills());
        existingProfile.setExperienceYears(updatedProfile.getExperienceYears());
        existingProfile.setExperienceDescription(updatedProfile.getExperienceDescription());
        existingProfile.setPreferredCategories(updatedProfile.getPreferredCategories());
        existingProfile.setHourlyRate(updatedProfile.getHourlyRate());
        existingProfile.setAvailabilityStatus(updatedProfile.getAvailabilityStatus());
        existingProfile.setIsPublic(updatedProfile.getIsPublic());
        existingProfile.setUpdatedAt(LocalDateTime.now());
        existingProfile.setLastActive(LocalDateTime.now());
        
        // Save updated profile
        Profile savedProfile = profileRepository.save(existingProfile);
        
        log.info("✅ Profile updated successfully: {} (ID: {})", savedProfile.getDisplayName(), savedProfile.getId());
        
        return savedProfile;
    }
    
    /**
     * Delete profile
     */
    public void deleteProfile(Long profileId, Long userId) {
        log.info("🗑️ Deleting profile ID: {} by user ID: {}", profileId, userId);
        
        // Check if profile exists and user owns it
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Check if user owns this profile
        if (!profile.getUserId().equals(userId)) {
            log.warn("❌ User {} is not authorized to delete profile ID: {}", userId, profileId);
            throw new RuntimeException("You are not authorized to delete this profile");
        }
        
        profileRepository.deleteById(profileId);
        
        log.info("✅ Profile deleted successfully: {} (ID: {})", profile.getDisplayName(), profileId);
    }
    
    /**
     * Add rating to profile
     */
    public Profile addRating(Long profileId, BigDecimal rating) {
        log.info("⭐ Adding rating {} to profile ID: {}", rating, profileId);
        
        // Validate rating
        validateRating(rating);
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Add rating
        profile.addRating(rating);
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Rating added successfully: {} to profile: {} (New rating: {})", 
                rating, savedProfile.getDisplayName(), savedProfile.getRating());
        
        return savedProfile;
    }
    
    /**
     * Mark task as completed
     */
    public Profile markTaskCompleted(Long profileId) {
        log.info("✅ Marking task as completed for profile ID: {}", profileId);
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Update completed tasks count
        profile.setCompletedTasks(profile.getCompletedTasks() + 1);
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Task marked as completed for profile: {} (Total completed: {})", 
                savedProfile.getDisplayName(), savedProfile.getCompletedTasks());
        
        return savedProfile;
    }
    
    /**
     * Mark task as successful
     */
    public Profile markTaskSuccessful(Long profileId) {
        log.info("🎉 Marking task as successful for profile ID: {}", profileId);
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Update successful tasks count
        profile.setSuccessfulTasks(profile.getSuccessfulTasks() + 1);
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Task marked as successful for profile: {} (Total successful: {})", 
                savedProfile.getDisplayName(), savedProfile.getSuccessfulTasks());
        
        return savedProfile;
    }
    
    /**
     * Add earnings to profile
     */
    public Profile addEarnings(Long profileId, BigDecimal amount) {
        log.info("💰 Adding earnings ${} to profile ID: {}", amount, profileId);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("❌ Invalid earnings amount: {}", amount);
            throw new RuntimeException("Earnings amount must be positive");
        }
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Add earnings
        profile.addEarnings(amount);
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Earnings added successfully: ${} to profile: {} (Total earnings: ${})", 
                amount, savedProfile.getDisplayName(), savedProfile.getTotalEarnings());
        
        return savedProfile;
    }
    
    /**
     * Verify profile
     */
    public Profile verifyProfile(Long profileId) {
        log.info("✅ Verifying profile ID: {}", profileId);
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Verify profile
        profile.verifyProfile();
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Profile verified successfully: {} (ID: {})", savedProfile.getDisplayName(), savedProfile.getId());
        
        return savedProfile;
    }
    
    /**
     * Update availability status
     */
    public Profile updateAvailabilityStatus(Long profileId, Profile.AvailabilityStatus status, Long userId) {
        log.info("🔄 Updating availability status to {} for profile ID: {} by user ID: {}", status, profileId, userId);
        
        // Check if profile exists and user owns it
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Check if user owns this profile
        if (!profile.getUserId().equals(userId)) {
            log.warn("❌ User {} is not authorized to update profile ID: {}", userId, profileId);
            throw new RuntimeException("You are not authorized to update this profile");
        }
        
        // Update availability status
        profile.setAvailabilityStatus(status);
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Availability status updated successfully: {} for profile: {} (ID: {})", 
                status, savedProfile.getDisplayName(), savedProfile.getId());
        
        return savedProfile;
    }
    
    /**
     * Update last active time
     */
    public Profile updateLastActive(Long profileId) {
        log.info("⏰ Updating last active time for profile ID: {}", profileId);
        
        Optional<Profile> profileOpt = profileRepository.findById(profileId);
        
        if (profileOpt.isEmpty()) {
            log.warn("❌ Profile not found with ID: {}", profileId);
            throw new RuntimeException("Profile not found");
        }
        
        Profile profile = profileOpt.get();
        
        // Update last active time
        profile.updateLastActive();
        
        Profile savedProfile = profileRepository.save(profile);
        
        log.info("✅ Last active time updated for profile: {} (ID: {})", savedProfile.getDisplayName(), savedProfile.getId());
        
        return savedProfile;
    }
    
    /**
     * Get profile statistics
     */
    public ProfileStatistics getProfileStatistics() {
        log.info("📊 Retrieving profile statistics");
        
        long totalProfiles = profileRepository.count();
        long verifiedProfiles = profileRepository.countByIsVerifiedTrue();
        long publicProfiles = profileRepository.countByIsPublicTrue();
        long availableProfiles = profileRepository.countByAvailabilityStatus(Profile.AvailabilityStatus.AVAILABLE);
        long busyProfiles = profileRepository.countByAvailabilityStatus(Profile.AvailabilityStatus.BUSY);
        long unavailableProfiles = profileRepository.countByAvailabilityStatus(Profile.AvailabilityStatus.UNAVAILABLE);
        
        ProfileStatistics stats = ProfileStatistics.builder()
                .totalProfiles(totalProfiles)
                .verifiedProfiles(verifiedProfiles)
                .publicProfiles(publicProfiles)
                .availableProfiles(availableProfiles)
                .busyProfiles(busyProfiles)
                .unavailableProfiles(unavailableProfiles)
                .build();
        
        log.info("✅ Profile statistics retrieved: {}", stats);
        
        return stats;
    }
    
    /**
     * Validate profile data
     */
    private void validateProfileData(Profile profile) {
        if (profile.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        
        if (profile.getUserEmail() == null || profile.getUserEmail().trim().isEmpty()) {
            throw new RuntimeException("User email is required");
        }
        
        if (profile.getBio() != null && profile.getBio().length() > maxBioLength) {
            throw new RuntimeException("Bio cannot exceed " + maxBioLength + " characters");
        }
        
        if (profile.getSkills() != null && profile.getSkills().size() > maxSkillsCount) {
            throw new RuntimeException("Cannot have more than " + maxSkillsCount + " skills");
        }
        
        if (profile.getExperienceYears() != null && profile.getExperienceYears() > maxExperienceYears) {
            throw new RuntimeException("Experience years cannot exceed " + maxExperienceYears);
        }
        
        if (profile.getAcademicYear() != null && (profile.getAcademicYear() < 1 || profile.getAcademicYear() > 10)) {
            throw new RuntimeException("Academic year must be between 1 and 10");
        }
        
        if (profile.getHourlyRate() != null && profile.getHourlyRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Hourly rate cannot be negative");
        }
    }
    
    /**
     * Validate rating against configured scale
     */
    private void validateRating(BigDecimal rating) {
        try {
            String[] range = ratingScale.split("-");
            int minRating = Integer.parseInt(range[0]);
            int maxRating = Integer.parseInt(range[1]);
            
            if (rating.compareTo(BigDecimal.valueOf(minRating)) < 0 || rating.compareTo(BigDecimal.valueOf(maxRating)) > 0) {
                log.warn("❌ Invalid rating: {} (must be between {} and {})", rating, minRating, maxRating);
                throw new RuntimeException("Rating must be between " + minRating + " and " + maxRating);
            }
        } catch (NumberFormatException e) {
            log.error("❌ Invalid rating scale configuration: {}", ratingScale);
            throw new RuntimeException("Invalid rating scale configuration: " + ratingScale);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("❌ Invalid rating scale format: {} (expected format: min-max)", ratingScale);
            throw new RuntimeException("Invalid rating scale format: " + ratingScale + " (expected format: min-max)");
        }
    }
    
    /**
     * Get the configured rating scale
     */
    public String getRatingScale() {
        return ratingScale;
    }
    
    /**
     * Profile Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ProfileStatistics {
        private long totalProfiles;
        private long verifiedProfiles;
        private long publicProfiles;
        private long availableProfiles;
        private long busyProfiles;
        private long unavailableProfiles;
    }
}
