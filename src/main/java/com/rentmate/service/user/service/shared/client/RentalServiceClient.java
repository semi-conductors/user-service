package com.rentmate.service.user.service.shared.client;

import com.rentmate.service.user.domain.dto.rental.RentalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "rental-service",
        url = "${rental.service.url}"
)
public interface RentalServiceClient {
    @GetMapping("/rentals/{id}")
    RentalResponse getRentalById(@PathVariable Long id);
}