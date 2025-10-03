package com.rentmate.service.user.service.shared.specification;

import com.rentmate.service.user.domain.entity.VerificationRequest;
import com.rentmate.service.user.domain.enumuration.VerificationRequestStatus;
import org.springframework.data.jpa.domain.Specification;

public class VerificationRequestSpecification {
    public static Specification<VerificationRequest> withStatus(VerificationRequestStatus status){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status));
    }

    public static Specification<VerificationRequest> withUserId(Long id){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), id));
    }
}
