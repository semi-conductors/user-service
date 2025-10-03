package com.rentmate.service.user.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rentmate.service.user.service.CloudinaryService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> generateSignedParams(String publicId) {
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "verifications",
                "timestamp", timestamp
        );

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        params.put("signature", signature);
        params.put("api_key", cloudinary.config.apiKey);
        return params;
    }
}
