package com.rentmate.service.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private Integer currentPage;
    private Integer totalPages;
    private Long totalItems;
    private Integer itemsPerPage;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Iterable<T> items;
}