package com.rentmate.service.user.domain.dto.verification;

import com.rentmate.service.user.domain.dto.PagedResponse;

public class VerificationListResponse extends PagedResponse<VerificationResponse> {
    public VerificationListResponse(Integer currentPage, Integer totalPages, Long totalItems, Integer itemsPerPage, Boolean hasNext, Boolean hasPrevious, Iterable<VerificationResponse> items) {
        super(currentPage, totalPages, totalItems, itemsPerPage, hasNext, hasPrevious, items);
    }
}
