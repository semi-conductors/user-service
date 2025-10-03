package com.rentmate.service.user.domain.dto.verification;

import com.rentmate.service.user.domain.validation.ValidCloudinaryUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateVerificationRequest {
//    @ValidCloudinaryUrl
    @NotBlank
    private String idFrontImageUrl;

//    @ValidCloudinaryUrl
    @NotBlank
    private String idBackImageUrl;

    @Size(min = 14, max = 14, message = "ID number must be 14 digit long")
    @Pattern(regexp = "^[0-9]*$", message = "ID number must be numeric")
    private String idNumber;
}
