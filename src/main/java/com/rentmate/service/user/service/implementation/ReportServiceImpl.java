package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.rental.RentalResponse;
import com.rentmate.service.user.domain.dto.report.*;
import com.rentmate.service.user.domain.dto.user.UserPrincipal;
import com.rentmate.service.user.domain.dto.user.UserProfileResponse;
import com.rentmate.service.user.domain.dto.user.UsernameDto;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import com.rentmate.service.user.domain.mapper.EventMapper;
import com.rentmate.service.user.domain.mapper.ReportMapper;
import com.rentmate.service.user.domain.mapper.UserMapper;
import com.rentmate.service.user.repository.UserReportRepository;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.service.ReportService;
import com.rentmate.service.user.service.UserEventPublisher;
import com.rentmate.service.user.service.UserService;
import com.rentmate.service.user.service.shared.client.RentalServiceClient;
import com.rentmate.service.user.service.shared.exception.BadRequestException;
import com.rentmate.service.user.service.shared.exception.ForbiddenActionException;
import com.rentmate.service.user.service.shared.exception.NotFoundException;
import com.rentmate.service.user.service.shared.specification.ReportSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class ReportServiceImpl implements ReportService {
    private final UserReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;
    private final RentalServiceClient rentalService;

    @Value("${report.locking-period-minutes:30}")
    private Long lockingPeriodMinutes;

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        boolean isSystemReport = request.getReportType() == CreateReportRequest.ReportType.FAKE_USER
                || request.getReportType() == CreateReportRequest.ReportType.FRAUD;

        validateDetailsLength(request);
        RentalResponse rental = validateRental(request);

        User reporter = userRepository.findById(isSystemReport ? UserService.getAuthenticatedUserId() : rental.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Reporter user not found"));
        User reportedUser = userRepository.findById(rental.getRenterId())
                .orElseThrow(() -> new NotFoundException("Reported user not found"));

        UserReport savedReport = reportRepository.save(ReportMapper.toUserReport(request, reporter, reportedUser));
        updateReportedUserStatus(reportedUser, request.getReportType());

        eventPublisher.publishReportCreatedEvent(EventMapper.toReportCreatedEvent(savedReport));

        return ReportMapper.toReportResponse(savedReport);
    }

    @Override
    public ReportListResponse getSubmittedReports(UserPrincipal loggedInUser, int page, int limit) {
        Pageable pageable = PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "submittedAt"));

        Page<UserReport> reports = reportRepository.findByReporterId(loggedInUser.getId(), pageable);

        if(reports.isEmpty()) ReportListResponse.empty();

        return new ReportListResponse(
                page,
                reports.getTotalPages(),
                reports.getTotalElements(),
                limit,
                reports.hasNext(),
                reports.hasPrevious(),
                getReportResponsesWithRelatedUsers(reports.getContent())
        );
    }

    @Override
    public ReportListResponse getReceivedReports(UserPrincipal loggedInUser, int page, int limit) {
        Pageable pageable = PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "submittedAt"));

        Page<UserReport> reports = reportRepository.findByReportedId(loggedInUser.getId(), pageable);

        if(reports.isEmpty()) ReportListResponse.empty();

        return new ReportListResponse(
                page,
                reports.getTotalPages(),
                reports.getTotalElements(),
                limit,
                reports.hasNext(),
                reports.hasPrevious(),
                getReportResponsesWithRelatedUsers(reports.getContent())
        );
    }

    @Override
    public ReportListResponse getAllReports(int page, int limit, ReportStatus status, ReportType type) {
        Pageable pageable = PageRequest.of(page-1, limit, Sort.by(Sort.Direction.DESC, "submittedAt"));

        Specification<UserReport> spec = Specification.unrestricted();

        if(status != null) spec = spec.and(ReportSpecification.withStatus(status));
        if(type != null) spec = spec.and(ReportSpecification.withType(type));

        Page<UserReport> reports = reportRepository.findAll(spec, pageable);

        if(reports.isEmpty()) return ReportListResponse.empty();

        return new ReportListResponse(
                page,
                reports.getTotalPages(),
                reports.getTotalElements(),
                limit,
                reports.hasNext(),
                reports.hasPrevious(),
                getReportResponsesWithRelatedUsers(reports.getContent())
        );
    }

    @Override
    public ReportDetailsResponse getReport(Long id) {
        UserReport report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));

        var res = userRepository.findAllById(List.of(report.getReporter().getId(), report.getReportedUser().getId()));
        UserProfileResponse reporter = UserMapper.toUserProfileResponse(res
                .stream()
                .filter(user -> user.getId().equals(report.getReporter().getId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Reporter not found")));

        UserProfileResponse reported = UserMapper.toUserProfileResponse(res
                .stream()
                .filter(user -> user.getId().equals(report.getReportedUser().getId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Reported user not found")));

        return ReportMapper.toReportDetailsResponse(report, reporter, reported);
    }

    @Override @Transactional
    public void claimReport(Long id, UserPrincipal loggedInUser) {
        UserReport report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Report not found"));

        if(report.getStatus() == ReportStatus.DISMISSED || report.getStatus() == ReportStatus.RESOLVED)
            throw new BadRequestException("Only PENDING reports can be locked");

        if(report.isLocked() && !report.isLockedBy(loggedInUser.getId()))
            throw new BadRequestException("Report is locked by another admin until: " + report.getLockExpiresAt());


        User user =  new User(); user.setId(loggedInUser.getId());

        report.setClaimedAt(LocalDateTime.now());
        report.setClaimedBy(user);
        report.setLockExpiresAt(LocalDateTime.now().plusMinutes(lockingPeriodMinutes));
        report.setStatus(ReportStatus.UNDER_REVIEW);

        reportRepository.save(report);
    }

    @Override @Transactional
    public void releaseReport(Long id, UserPrincipal loggedInUser) {
        UserReport report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Report not found"));

        if(!report.isLockedBy(loggedInUser.getId()))
            throw new ForbiddenActionException("You don't have a lock on this report");

        report.setClaimedBy(null);
        report.setClaimedAt(null);
        report.setLockExpiresAt(null);
        report.setStatus(ReportStatus.PENDING);

        reportRepository.save(report);
    }

    @Override @Transactional
    public void resolveReport(Long id, String message, UserPrincipal loggedInUser, boolean dismissed) {
        UserReport report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Report not found"));

        if(report.getStatus() == ReportStatus.RESOLVED || report.getStatus() == ReportStatus.DISMISSED)
            throw new BadRequestException("This report have been resolved");

        if(report.isLocked() && !report.isLockedBy(loggedInUser.getId()))
            throw new BadRequestException("Report is locked by another admin until: " + report.getLockExpiresAt());

        if(dismissed){
            User reported = report.getReportedUser();

            if(reported.getActivityStatus() == AccountActivityStatus.PENDING_REPORT_REVIEW)
                reported.setActivityStatus(AccountActivityStatus.ACTIVE);

            userRepository.save(reported);
        }


        User resolver = new User(); resolver.setId(loggedInUser.getId());

        report.setStatus(dismissed ? ReportStatus.DISMISSED : ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(resolver);
        report.setResolutionNotes(message);
        report.setClaimedAt(null);
        report.setClaimedBy(null);
        report.setLockExpiresAt(null);

        reportRepository.save(report);
    }

    @Override @Transactional
    public LocalDateTime refreshLock(Long reportId, UserPrincipal loggedInUser) {
        UserReport report = reportRepository.findById(reportId).orElseThrow(() -> new NotFoundException("Report not found"));

        if(!report.isLockedBy(loggedInUser.getId()))
            throw new ForbiddenActionException("You don't have a lock on this report");

        report.setLockExpiresAt(LocalDateTime.now().plusMinutes(lockingPeriodMinutes));
        reportRepository.save(report);

        return report.getLockExpiresAt();
    }

    private void validateDetailsLength(CreateReportRequest request) {
        int minLength = switch (request.getReportType()) {
            case FAKE_USER, OVERDUE -> 20;
            case FRAUD, DAMAGE -> 60;
        };

        if (request.getDetails() == null || request.getDetails().length() < minLength) {
            throw new BadRequestException(
                    String.format("Details must be at least %d characters for %s reports",
                            minLength, request.getReportType())
            );
        }
    }

    private RentalResponse validateRental(CreateReportRequest request) {
        RentalResponse rental ;
        try{
             rental = rentalService.getRentalById(request.getRelatedRentalId());
        } catch (Exception e) {
            log.error("Error while validating delivery ID", e);
            throw e;
        }

        if(rental == null)
            throw new NotFoundException("No rental with the id = " + request.getRelatedRentalId());


        // For OVERDUE reports, validate rental is actually overdue
        if (request.getReportType() == CreateReportRequest.ReportType.OVERDUE
            && rental.getEndDate().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Rental is not overdue.");

        return rental;
    }

    @Transactional
    private void updateReportedUserStatus(User reportedUser, CreateReportRequest.ReportType reportType) {
        reportedUser.setActivityStatus(AccountActivityStatus.PENDING_REPORT_REVIEW);
        userRepository.save(reportedUser);
    }

    private List<ReportResponse> getReportResponsesWithRelatedUsers(List<UserReport> reports) {
        var reporterUsers = getRelatedUsers(reports, true);
        var reportedUsers = getRelatedUsers(reports, false);

        List<ReportResponse> items = new ArrayList<>();
        for (UserReport report : reports) {
            items.add(ReportMapper.toReportResponse(report, reporterUsers.get(report.getReporter().getId()), reportedUsers.get(report.getReportedUser().getId())));
        }

        return items;
    }

    private HashMap<Long, UsernameDto> getRelatedUsers(List<UserReport> reports, boolean isReporter) {
        HashSet<Long> reportedUserIds = new HashSet<>();
        if(isReporter)
            reports.forEach(report -> reportedUserIds.add(report.getReporter().getId()));
        else
            reports.forEach(report -> reportedUserIds.add(report.getReportedUser().getId()));

        List<UsernameDto> usernameDtos = userRepository.findByIdIn(reportedUserIds, UsernameDto.class);

        return usernameDtos
                .stream()
                .collect(
                        HashMap::new,
                        (map, usernameDto) -> map.put(usernameDto.getId(), usernameDto),
                        HashMap::putAll
                );
    }
}