package com.rentmate.service.user.repository;

import com.rentmate.service.user.domain.dto.report.ReportResponse;
import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserReportRepository extends JpaRepository<UserReport, Long>, JpaSpecificationExecutor<UserReport> {

    @Query("SELECT ur FROM UserReport ur WHERE ur.submittedAt <= :dateTime AND ur.reportType = :reportType AND ur.status = :reportStatus")
    List<UserReport> findReportsSubbmitedBefore(LocalDateTime dateTime, ReportType reportType, ReportStatus reportStatus);

    @Query("SELECT ur FROM UserReport ur WHERE ur.reporter.id = :id")
    Page<UserReport> findByReporterId(Long id, Pageable pageable);

    @Query("SELECT ur FROM UserReport ur WHERE ur.reportedUser.id = :id")
    Page<UserReport> findByReportedId(Long id, Pageable pageable);

    @Query("SELECT r FROM UserReport r WHERE r.claimedBy IS NOT NULL AND r.lockExpiresAt < :now")
    List<UserReport> findExpiredLocks(LocalDateTime now);
}
