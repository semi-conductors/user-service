package com.rentmate.service.user.service;

import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> generateSignedParams(String publicId);
}
