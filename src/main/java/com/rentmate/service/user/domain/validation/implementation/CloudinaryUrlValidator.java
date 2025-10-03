package com.rentmate.service.user.domain.validation.implementation;

import com.rentmate.service.user.domain.validation.ValidCloudinaryUrl;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Component;


import java.util.Map;

@Component
public class CloudinaryUrlValidator implements ConstraintValidator<ValidCloudinaryUrl, String> {

    private Cloudinary cloudinary;
    public CloudinaryUrlValidator(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        try {
            // Extract public_id from the Cloudinary URL
            // Example URL: https://res.cloudinary.com/<cloud_name>/image/upload/v1234567890/sample.jpg
            String[] parts = url.split("/");
            String publicIdWithExt = parts[parts.length - 1]; // sample.jpg
            String publicId = publicIdWithExt.contains(".")
                    ? publicIdWithExt.substring(0, publicIdWithExt.lastIndexOf("."))
                    : publicIdWithExt;

            // Call Cloudinary API to verify resource exists
            Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());

            return result != null && !result.isEmpty();
        } catch (Exception e) {
            // If resource not found or invalid
            return false;
        }
    }
}

