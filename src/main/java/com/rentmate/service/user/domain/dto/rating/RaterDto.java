package com.rentmate.service.user.domain.dto.rating;

import lombok.Data;

@Data
public class RaterDto {
    private final Long id;
    private final String firstName;
    private final String lastName;

    public RaterDto(Long id, String firstName, String lastName) {
        this.id = id; this.firstName = firstName; this.lastName = lastName;
    }
}