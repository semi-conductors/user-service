package com.rentmate.service.user.domain.entity;

import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        })
@Data
public class VerificationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_front_image_url", nullable = false, columnDefinition = "TEXT")
    private String idFrontImageUrl;

    @Column(name = "id_back_image_url", nullable = false, columnDefinition = "TEXT")
    private String idBackImageUrl;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING'")
    private VerificationRequestStatus status = VerificationRequestStatus.PENDING;

    @Column(name = "submitted_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "can_resubmit_after")
    private LocalDateTime canResubmitAfter;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by",
            foreignKey = @ForeignKey(name = "fk_verification_reviewer"))
    private User reviewedBy;
}

