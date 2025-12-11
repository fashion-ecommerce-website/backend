package com.spring.fit.backend.user.repository;

import com.spring.fit.backend.user.domain.entity.UserMeasurements;
import com.spring.fit.backend.user.domain.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMeasurementsRepository extends JpaRepository<UserMeasurements, Long> {

    Optional<UserMeasurements> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);

    // Find similar users based on profile and measurements criteria
    @Query("SELECT um FROM UserMeasurements um WHERE " +
            "um.gender = :gender AND " +
            "um.bmi BETWEEN :bmiMin AND :bmiMax AND " +
            "um.height BETWEEN :heightMin AND :heightMax AND " +
            "um.user.id != :excludeUserId")
    List<UserMeasurements> findSimilarUsers(
            @Param("gender") Gender gender,
            @Param("bmiMin") BigDecimal bmiMin,
            @Param("bmiMax") BigDecimal bmiMax,
            @Param("heightMin") BigDecimal heightMin,
            @Param("heightMax") BigDecimal heightMax,
            @Param("excludeUserId") Long excludeUserId);
    
    /**
     * Find users in same cluster (optimized for clustering approach)
     * Filters by gender, BMI range, age range, and outliers
     */
    @Query("SELECT um FROM UserMeasurements um WHERE " +
            "um.gender = :gender AND " +
            "um.bmi BETWEEN :bmiMin AND :bmiMax AND " +
            "um.age BETWEEN :ageMin AND :ageMax AND " +
            "um.user.id != :excludeUserId AND " +
            "um.bmi BETWEEN 15.0 AND 40.0 AND " +
            "um.height BETWEEN 140 AND 220 AND " +
            "um.weight BETWEEN 30 AND 200")
    List<UserMeasurements> findUsersInCluster(
            @Param("gender") Gender gender,
            @Param("bmiMin") BigDecimal bmiMin,
            @Param("bmiMax") BigDecimal bmiMax,
            @Param("ageMin") Integer ageMin,
            @Param("ageMax") Integer ageMax,
            @Param("excludeUserId") Long excludeUserId);
}
