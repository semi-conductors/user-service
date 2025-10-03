package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long>, JpaSpecificationExecutor<VerificationRequest> {
    @Query("""
            SELECT true FROM VerificationRequest vr
            WHERE (vr.status = 'PENDING' OR vr.status = 'APPROVED')
            AND vr.user.id = ?1
            """)
    Optional<Boolean> anyPendingOrApprovedRequestForUser(Long userId);

    @Query("""
            SELECT MAX(vr.reviewedAt) FROM VerificationRequest vr
            WHERE vr.status = com.rentmate.service.user.domain.enumuration.VerificationRequestStatus.REJECTED
            AND vr.user.id = ?1
            """)
    Optional<LocalDateTime> findLastRejectionTimeForUser(Long userId);

    @Query("""
            SELECT vr FROM VerificationRequest vr JOIN FETCH vr.user LEFT JOIN FETCH vr.reviewedBy
            WHERE vr.id = ?1
            """)
    Optional<VerificationRequest> findByIdWithDetails(Long id);
}
