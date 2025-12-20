package com.spring.fit.backend.user.domain.entity;

import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.user.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_measurements", indexes = {
        @Index(name = "idx_user_measurements_user_id", columnList = "user_id"),
        @Index(name = "idx_user_measurements_gender_bmi", columnList = "gender, bmi"),
        @Index(name = "idx_user_measurements_bmi", columnList = "bmi")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeasurements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "height", nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "chest", nullable = false, precision = 5, scale = 2)
    private BigDecimal chest;

    @Column(name = "waist", nullable = false, precision = 5, scale = 2)
    private BigDecimal waist;

    @Column(name = "hips", nullable = false, precision = 5, scale = 2)
    private BigDecimal hips;

    @Column(name = "bmi", nullable = false, precision = 4, scale = 2)
    private BigDecimal bmi;

    @Enumerated(EnumType.STRING)
    @Column(name = "belly_shape", length = 10)
    private BellyShape bellyShape;

    @Enumerated(EnumType.STRING)
    @Column(name = "hip_shape", length = 10)
    private HipShape hipShape;

    @Enumerated(EnumType.STRING)
    @Column(name = "chest_shape", length = 10)
    private ChestShape chestShape;

    @Enumerated(EnumType.STRING)
    @Column(name = "fit_preference", length = 15)
    private FitPreference fitPreference;

    @Column(name = "has_return_history")
    @Builder.Default
    private Boolean hasReturnHistory = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
