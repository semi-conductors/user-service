package com.rentmate.service.user.service.shared.specification;

import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> hasRole(UserRole role){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(AccountActivityStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("activityStatus"), status);
    }

    public static Specification<User> isVerified(Boolean isVerified) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isIdentityVerified"), isVerified);
    }

    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + keyword.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern));

            // Handle nullable phone number
            predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("phoneNumber")),
                    criteriaBuilder.like(root.get("phoneNumber"), likePattern)
            ));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
