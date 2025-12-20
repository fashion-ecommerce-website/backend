package com.spring.fit.backend.security.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Entity
@Table(name = "users", indexes = {
		@Index(name = "idx_user_email", columnList = "email", unique = true),
		@Index(name = "idx_user_username", columnList = "username", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "dob")
	private LocalDate dob;

	@Pattern(regexp = "^[0-9]{10,11}$", message = "Invalid phone number format")
	@Column(length = 15)
	private String phone;

	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	@Default
	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@Column(length = 500)
	private String reason;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "email_verified", nullable = false)
	@Default
	private boolean emailVerified = false;

	@Column(name = "phone_verified", nullable = false)
	@Default
	private boolean phoneVerified = false;

	@Column(name = "rank_id")
    @Default
	private Short rankId = 1;

	// Spending tracking field
	@Column(name = "total_spent", precision = 12, scale = 2)
	@Default
	private BigDecimal totalSpent = BigDecimal.ZERO;

	// Password reset support
	@Column(name = "reset_password_token", length = 255)
	private String resetPasswordToken;

	@Column(name = "reset_password_expires_at")
	private LocalDateTime resetPasswordTokenExpiresAt;

	// Relationships
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<UserRoleEntity> userRoles = new LinkedHashSet<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<RefreshTokenEntity> refreshTokens = new LinkedHashSet<>();

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<com.spring.fit.backend.voucher.domain.entity.VoucherUsage> voucherUsages = new LinkedHashSet<>();
}
