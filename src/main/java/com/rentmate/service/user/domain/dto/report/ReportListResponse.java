package com.rentmate.service.user.domain.dto.report;

import com.rentmate.service.user.domain.dto.PagedResponse;

import java.util.ArrayList;

public class ReportListResponse extends PagedResponse<ReportResponse> {
    public ReportListResponse(Integer currentPage, Integer totalPages, Long totalItems, Integer itemsPerPage, Boolean hasNext, Boolean hasPrevious, Iterable<ReportResponse> items) {
        super(currentPage, totalPages, totalItems, itemsPerPage, hasNext, hasPrevious, items);
    }

    public static ReportListResponse empty() {
        return new ReportListResponse(
                0,
                0,
                0L,
                0,
                false,
                false,
                new ArrayList<>()
        );
    }
}
