package com.rentmate.service.user.domain.entity;

import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role = UserRole.USER;

    @Column(name = "account_activity_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountActivityStatus activityStatus = AccountActivityStatus.ACTIVE;

    @Column(name = "is_identity_verified", nullable = false)
    private boolean isIdentityVerified;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "is_phone_number_verified", nullable = false)
    private boolean isPhoneVerified ;

    @Column(name = "is_disabled", nullable = false)
    private boolean isDisabled;

    @Column(name = "average_rating", nullable = false)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_ratings", nullable = false)
    private Integer totalRating = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
