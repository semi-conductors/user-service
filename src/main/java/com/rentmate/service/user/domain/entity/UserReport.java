package com.rentmate.service.user.domain.entity;

import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
@Data
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_report_reporter"))
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_report_reported"))
    private User reportedUser;

    // Cross-Service Links
    @Column(name = "related_rental_id")
    private Long relatedRentalId;

    @Column(name = "related_delivery_id")
    private Long relatedDeliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    private ReportType reportType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String details;

    // JSON column for evidence URLs
    @Column(name = "evidence_urls", columnDefinition = "JSON")
    private String evidenceUrls;

    // Admin Management Fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "ENUM('PENDING_OWNER_VERIFICATION','PENDING','UNDER_REVIEW','RESOLVED','DISMISSED') DEFAULT 'PENDING'")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "damage_percentage", precision = 5, scale = 2)
    private BigDecimal damagePercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", foreignKey = @ForeignKey(name = "fk_report_assigned_to"))
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by", foreignKey = @ForeignKey(name = "fk_report_resolved_by"))
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_escalated_from", foreignKey = @ForeignKey(name = "fk_report_escalation"))
    private UserReport autoEscalatedFrom;


    @Column(name = "submitted_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by")
    private User claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;

    public boolean isLocked() {
        return claimedBy != null && lockExpiresAt != null && lockExpiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isLockedBy(Long adminId) {
        return isLocked() && claimedBy.getId().equals(adminId);
    }
}

