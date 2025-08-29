package com.campusworks.profile.repo;

import com.campusworks.profile.model.Profile;
import com.campusworks.profile.model.Profile.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Profile Repository
 * Handles database operations for Profile entities
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    /**
     * Find profile by user ID
     */
    Optional<Profile> findByUserId(Long userId);
    
    /**
     * Find profile by user email
     */
    Optional<Profile> findByUserEmail(String userEmail);
    
    /**
     * Find all public profiles
     */
    List<Profile> findByIsPublicTrueOrderByRatingDesc();
    
    /**
     * Find all verified profiles
     */
    List<Profile> findByIsVerifiedTrueOrderByRatingDesc();
    
    /**
     * Find profiles by availability status
     */
    List<Profile> findByAvailabilityStatusOrderByRatingDesc(AvailabilityStatus status);
    
    /**
     * Find profiles by university
     */
    List<Profile> findByUniversityOrderByRatingDesc(String university);
    
    /**
     * Find profiles by major
     */
    List<Profile> findByMajorOrderByRatingDesc(String major);
    
    /**
     * Find profiles by academic year
     */
    List<Profile> findByAcademicYearOrderByRatingDesc(Integer academicYear);
    
    /**
     * Find profiles with rating above a certain value
     */
    @Query("SELECT p FROM Profile p WHERE p.rating >= :minRating ORDER BY p.rating DESC")
    List<Profile> findByRatingAbove(@Param("minRating") BigDecimal minRating);
    
    /**
     * Find profiles with rating below a certain value
     */
    @Query("SELECT p FROM Profile p WHERE p.rating <= :maxRating ORDER BY p.rating DESC")
    List<Profile> findByRatingBelow(@Param("maxRating") BigDecimal maxRating);
    
    /**
     * Find profiles by rating range
     */
    @Query("SELECT p FROM Profile p WHERE p.rating BETWEEN :minRating AND :maxRating ORDER BY p.rating DESC")
    List<Profile> findByRatingRange(@Param("minRating") BigDecimal minRating, @Param("maxRating") BigDecimal maxRating);
    
    /**
     * Find profiles by hourly rate range
     */
    @Query("SELECT p FROM Profile p WHERE p.hourlyRate BETWEEN :minRate AND :maxRate ORDER BY p.hourlyRate ASC")
    List<Profile> findByHourlyRateRange(@Param("minRate") BigDecimal minRate, @Param("maxRate") BigDecimal maxRate);
    
    /**
     * Find profiles by experience years
     */
    List<Profile> findByExperienceYearsOrderByRatingDesc(Integer experienceYears);
    
    /**
     * Find profiles with experience above a certain value
     */
    @Query("SELECT p FROM Profile p WHERE p.experienceYears >= :minYears ORDER BY p.experienceYears DESC")
    List<Profile> findByExperienceYearsAbove(@Param("minYears") Integer minYears);
    
    /**
     * Find profiles by skills (contains any of the skills)
     */
    @Query("SELECT p FROM Profile p JOIN p.skills s WHERE s IN :skills ORDER BY p.rating DESC")
    List<Profile> findBySkillsContaining(@Param("skills") List<String> skills);
    
    /**
     * Find profiles by specific skill
     */
    @Query("SELECT p FROM Profile p JOIN p.skills s WHERE s = :skill ORDER BY p.rating DESC")
    List<Profile> findBySkill(@Param("skill") String skill);
    
    /**
     * Find profiles by preferred categories
     */
    @Query("SELECT p FROM Profile p JOIN p.preferredCategories c WHERE c IN :categories ORDER BY p.rating DESC")
    List<Profile> findByPreferredCategoriesContaining(@Param("categories") List<String> categories);
    
    /**
     * Find profiles by preferred category
     */
    @Query("SELECT p FROM Profile p JOIN p.preferredCategories c WHERE c = :category ORDER BY p.rating DESC")
    List<Profile> findByPreferredCategory(@Param("category") String category);
    
    /**
     * Find profiles by completed tasks count
     */
    @Query("SELECT p FROM Profile p WHERE p.completedTasks >= :minTasks ORDER BY p.completedTasks DESC")
    List<Profile> findByCompletedTasksAbove(@Param("minTasks") Integer minTasks);
    
    /**
     * Find profiles by successful tasks count
     */
    @Query("SELECT p FROM Profile p WHERE p.successfulTasks >= :minTasks ORDER BY p.successfulTasks DESC")
    List<Profile> findBySuccessfulTasksAbove(@Param("minTasks") Integer minTasks);
    
    /**
     * Find profiles by total earnings
     */
    @Query("SELECT p FROM Profile p WHERE p.totalEarnings >= :minEarnings ORDER BY p.totalEarnings DESC")
    List<Profile> findByTotalEarningsAbove(@Param("minEarnings") BigDecimal minEarnings);
    
    /**
     * Find profiles by total earnings range
     */
    @Query("SELECT p FROM Profile p WHERE p.totalEarnings BETWEEN :minEarnings AND :maxEarnings ORDER BY p.totalEarnings DESC")
    List<Profile> findByTotalEarningsRange(@Param("minEarnings") BigDecimal minEarnings, @Param("maxEarnings") BigDecimal maxEarnings);
    
    /**
     * Find profiles that are available for work
     */
    @Query("SELECT p FROM Profile p WHERE p.isPublic = true AND p.isVerified = true AND p.availabilityStatus = 'AVAILABLE' ORDER BY p.rating DESC")
    List<Profile> findAvailableForWork();
    
    /**
     * Find profiles by multiple criteria
     */
    @Query("SELECT p FROM Profile p WHERE " +
           "(:university IS NULL OR p.university = :university) AND " +
           "(:major IS NULL OR p.major = :major) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating) AND " +
           "(:maxHourlyRate IS NULL OR p.hourlyRate <= :maxHourlyRate) AND " +
           "(:minExperience IS NULL OR p.experienceYears >= :minExperience) AND " +
           "p.isPublic = true " +
           "ORDER BY p.rating DESC")
    List<Profile> findByMultipleCriteria(
            @Param("university") String university,
            @Param("major") String major,
            @Param("minRating") BigDecimal minRating,
            @Param("maxHourlyRate") BigDecimal maxHourlyRate,
            @Param("minExperience") Integer minExperience);
    
    /**
     * Find top rated profiles
     */
    @Query("SELECT p FROM Profile p WHERE p.rating IS NOT NULL AND p.isPublic = true ORDER BY p.rating DESC")
    List<Profile> findTopRatedProfiles();
    
    /**
     * Find most experienced profiles
     */
    @Query("SELECT p FROM Profile p WHERE p.experienceYears IS NOT NULL AND p.isPublic = true ORDER BY p.experienceYears DESC")
    List<Profile> findMostExperiencedProfiles();
    
    /**
     * Find most successful profiles
     */
    @Query("SELECT p FROM Profile p WHERE p.successfulTasks IS NOT NULL AND p.isPublic = true ORDER BY p.successfulTasks DESC")
    List<Profile> findMostSuccessfulProfiles();
    
    /**
     * Find highest earning profiles
     */
    @Query("SELECT p FROM Profile p WHERE p.totalEarnings IS NOT NULL AND p.isPublic = true ORDER BY p.totalEarnings DESC")
    List<Profile> findHighestEarningProfiles();
    
    /**
     * Find profiles by last active time
     */
    @Query("SELECT p FROM Profile p WHERE p.lastActive >= :since ORDER BY p.lastActive DESC")
    List<Profile> findRecentlyActive(@Param("since") java.time.LocalDateTime since);
    
    /**
     * Find profiles created within a date range
     */
    @Query("SELECT p FROM Profile p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Profile> findByCreatedDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                       @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Find profiles updated within a date range
     */
    @Query("SELECT p FROM Profile p WHERE p.updatedAt BETWEEN :startDate AND :endDate ORDER BY p.updatedAt DESC")
    List<Profile> findByUpdatedDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                       @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Count profiles by university
     */
    long countByUniversity(String university);
    
    /**
     * Count profiles by major
     */
    long countByMajor(String major);
    
    /**
     * Count profiles by availability status
     */
    long countByAvailabilityStatus(AvailabilityStatus status);
    
    /**
     * Count verified profiles
     */
    long countByIsVerifiedTrue();
    
    /**
     * Count public profiles
     */
    long countByIsPublicTrue();
    
    /**
     * Count profiles with rating above a certain value
     */
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.rating >= :minRating")
    long countByRatingAbove(@Param("minRating") BigDecimal minRating);
    
    /**
     * Count profiles by academic year
     */
    long countByAcademicYear(Integer academicYear);
    
    /**
     * Check if profile exists by user ID
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Check if profile exists by user email
     */
    boolean existsByUserEmail(String userEmail);
    
    /**
     * Find profiles by multiple skills (must have all skills)
     */
    @Query("SELECT p FROM Profile p WHERE " +
           "EXISTS (SELECT 1 FROM p.skills s WHERE s = :skill1) AND " +
           "EXISTS (SELECT 1 FROM p.skills s WHERE s = :skill2) " +
           "ORDER BY p.rating DESC")
    List<Profile> findByMultipleSkills(@Param("skill1") String skill1, @Param("skill2") String skill2);
    
    /**
     * Find profiles by skill and rating combination
     */
    @Query("SELECT p FROM Profile p JOIN p.skills s WHERE s = :skill AND p.rating >= :minRating ORDER BY p.rating DESC")
    List<Profile> findBySkillAndRating(@Param("skill") String skill, @Param("minRating") BigDecimal minRating);
}
