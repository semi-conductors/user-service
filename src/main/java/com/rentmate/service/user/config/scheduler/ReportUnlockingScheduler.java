package com.rentmate.service.user.config.scheduler;

import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.repository.UserReportRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component @Slf4j
public class ReportUnlockingScheduler {
    private final UserReportRepository reportRepository;

    public ReportUnlockingScheduler(UserReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void releaseExpiredLocks() {
        log.info("Checking for expired report locks...");

        List<UserReport> expiredLocks = reportRepository.findExpiredLocks(LocalDateTime.now());

        for (var report : expiredLocks) {
            log.warn("Auto-releasing expired lock on report {} (was locked by admin {})",
                    report.getId(), report.getClaimedBy().getId());

            report.setClaimedBy(null);
            report.setClaimedAt(null);
            report.setLockExpiresAt(null);
            report.setStatus(ReportStatus.PENDING);

            reportRepository.save(report);
        }

        log.info("Released {} expired locks", expiredLocks.size());
    }
}
