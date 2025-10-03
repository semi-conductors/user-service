package com.rentmate.service.user.service;

import com.rentmate.service.user.domain.dto.verification.*;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface VerificationService {
    Map<String, Object> getUploadUrls();
    CanSubmitResponse canSubmit();
    VerificationResponse createVerification(CreateVerificationRequest request);
    VerificationDetailsResponse getVerification(Long id);
    VerificationResponse approveVerification(Long id);
    VerificationResponse rejectVerification(Long id, String reason);
    VerificationListResponse getAll(Integer page,Integer limit, VerificationRequestStatus status,
                                    String sortBy, String sortOrder);
    Iterable<VerificationResponse> getCurrentUserVerifications();
}
