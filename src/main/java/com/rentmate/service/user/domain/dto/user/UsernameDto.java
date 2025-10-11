package com.rentmate.service.user.domain.dto.user;

import lombok.Data;

@Data
public class UsernameDto {
    private Long id;
    private String firstName;
    private String lastName;

    public UsernameDto(Long id, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }
}
