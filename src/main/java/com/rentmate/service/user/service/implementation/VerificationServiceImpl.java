package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.domain.dto.user.UserEmailDto;
import com.rentmate.service.user.domain.dto.verification.*;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.entity.VerificationRequest;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import com.rentmate.service.user.domain.mapper.EventMapper;
import com.rentmate.service.user.domain.mapper.VerificationMapper;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.repository.VerificationRequestRepository;
import com.rentmate.service.user.service.CloudinaryService;
import com.rentmate.service.user.service.UserEventPublisher;
import com.rentmate.service.user.service.UserService;
import com.rentmate.service.user.service.VerificationService;
import com.rentmate.service.user.service.shared.exception.ForbiddenActionException;
import com.rentmate.service.user.service.shared.exception.NotFoundException;
import com.rentmate.service.user.service.shared.specification.VerificationRequestSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRepository;
    private final UserEventPublisher eventPublisher;
    @Override
    public Map<String, Object> getUploadUrls() {
        Map<String, Object> frontParams = cloudinaryService.generateSignedParams("id_front_" + UUID.randomUUID());
        Map<String, Object> backParams = cloudinaryService.generateSignedParams("id_back_" + UUID.randomUUID());

        return Map.of(
                "front", frontParams,
                "back", backParams
        );
    }

    @Override
    public CanSubmitResponse canSubmit() {
        Long userId = UserService.getAuthenticatedUserId();
        if (verificationRepository.anyPendingOrApprovedRequestForUser(userId).isPresent()) {
            return new CanSubmitResponse(false,
                    "You can't submit verification request for now because your already have one with PENDING/APPROVED status.",null);
        }

        Optional<LocalDateTime> lastRejectionTime = verificationRepository.findLastRejectionTimeForUser(userId);

        if(lastRejectionTime.isEmpty() || lastRejectionTime.get().plusHours(48).isBefore(LocalDateTime.now()))
            return new CanSubmitResponse(true, "You can submit verification request",LocalDateTime.now());

        return new CanSubmitResponse(false, "You can't submit verification request for now, 48 hours must be passed on the last rejection", lastRejectionTime.get().plusHours(48));
    }

    @Override
    public VerificationResponse createVerification(CreateVerificationRequest request) {
        if(!canSubmit().canSubmit())
            throw new ForbiddenActionException("You can't submit verification request for now");

        User user = new User();
        user.setId(UserService.getAuthenticatedUserId());
        VerificationRequest verificationRequest = VerificationMapper.toVerificationRequest(request, user);

        verificationRepository.save(verificationRequest);
        return VerificationMapper.toVerificationResponse(verificationRequest);
    }

    @Override
    public VerificationDetailsResponse getVerification(Long id) {
        VerificationRequest vr = verificationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Verification not found with the given id"));

        return VerificationMapper.toVerificationDetailsResponse(vr);
    }

    @Override @Transactional
    public VerificationResponse approveVerification(Long id) {
        VerificationRequest vr = verificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Verification not found with the given id"));

        User reviewer = new User();
        reviewer.setId(UserService.getAuthenticatedUserId());

        vr.setStatus(VerificationRequestStatus.APPROVED);
        vr.setReviewedBy(reviewer);
        vr.setReviewedAt(LocalDateTime.now());
        vr.setRejectionReason(null);

        verificationRepository.save(vr);
        userRepository.verifyUser(vr.getUser().getId());

        String email = userRepository.findById(vr.getUser().getId(), UserEmailDto.class)
                .orElseThrow(() -> new NotFoundException("user not found"))
                .getEmail();

        eventPublisher.publishIdentityVerificationApprovedEvent(
                EventMapper.toIdentityApprovedEvent(vr.getUser().getId(),email, vr));
        return VerificationMapper.toVerificationResponse(vr);
    }

    @Override
    public VerificationResponse rejectVerification(Long id, String reason) {
        VerificationRequest vr = verificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Verification not found with the given id"));

        User reviewer = new User();
        reviewer.setId(UserService.getAuthenticatedUserId());

        vr.setStatus(VerificationRequestStatus.REJECTED);
        vr.setReviewedBy(reviewer);
        vr.setReviewedAt(LocalDateTime.now());
        vr.setRejectionReason(reason);
        vr.setCanResubmitAfter(LocalDateTime.now().plusHours(48));

        verificationRepository.save(vr);

        String email = userRepository.findById(vr.getUser().getId(), UserEmailDto.class)
                .orElseThrow(() -> new NotFoundException("user not found"))
                .getEmail();

        eventPublisher.publishIdentityVerificationRejectedEvent(
                EventMapper.toIdentityRejectedEvent(vr.getUser().getId(),email, vr));
        return VerificationMapper.toVerificationResponse(vr);
    }

    @Override
    public VerificationListResponse getAll(Integer page, Integer limit, VerificationRequestStatus status,
                                           String sortBy, String sortOrder) {
        Pageable peagble = PageRequest.of(page-1, limit, mapSort(sortBy, sortOrder));

        Specification<VerificationRequest> spec = Specification.unrestricted();
        if(status != null)
            spec = spec.and(VerificationRequestSpecification.withStatus(status));

        Page<VerificationRequest> list = verificationRepository.findAll(spec,peagble);

        return new VerificationListResponse(
                page,
                list.getTotalPages(),
                list.getTotalElements(),
                limit,
                list.hasNext(),
                list.hasPrevious(),
                list.getContent().stream().map(VerificationMapper::toVerificationResponse).toList()
        );
    }

    @Override
    public Iterable<VerificationResponse> getCurrentUserVerifications() {
        Long userId = UserService.getAuthenticatedUserId();

        return verificationRepository
                .findAll(VerificationRequestSpecification.withUserId(userId))
                .stream()
                .map(VerificationMapper::toVerificationResponse)
                .toList();
    }

    private Sort mapSort(String sortBy, String sortOrder) {
        String sortByMapped = switch (sortBy.toLowerCase().trim()) {
            case "id" -> "id";
            case "reviewedat" -> "reviewedAt";
            default -> "createdAt";
        };

        return sortOrder.toLowerCase().trim().equals("desc") ?
                Sort.by(sortByMapped).descending() : Sort.by(sortByMapped).ascending();
    }
}