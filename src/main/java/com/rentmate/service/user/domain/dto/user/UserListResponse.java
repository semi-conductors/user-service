package com.rentmate.service.user.domain.dto.user;

import com.rentmate.service.user.domain.dto.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public class UserListResponse extends PagedResponse<UserProfileResponse> {
    public UserListResponse(List<UserProfileResponse> items, Integer currentPage, Integer totalPages,
                            Long totalItems, Integer itemsPerPage, Boolean hasNext, Boolean hasPrevious) {
        super(currentPage, totalPages, totalItems, itemsPerPage, hasNext, hasPrevious, items);
    }
}
