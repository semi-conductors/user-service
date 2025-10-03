package com.rentmate.service.user.domain.validation;

import com.rentmate.service.user.domain.validation.implementation.CloudinaryUrlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CloudinaryUrlValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCloudinaryUrl {
    String message() default "Invalid Cloudinary image URL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
