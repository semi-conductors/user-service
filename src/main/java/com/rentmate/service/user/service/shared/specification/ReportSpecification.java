package com.rentmate.service.user.service.shared.specification;

import com.rentmate.service.user.domain.entity.UserReport;
import com.rentmate.service.user.domain.enumuration.ReportStatus;
import com.rentmate.service.user.domain.enumuration.ReportType;
import org.springframework.data.jpa.domain.Specification;

public class ReportSpecification {
    public static Specification<UserReport> withStatus(ReportStatus status){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<UserReport> withType(ReportType type){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("reportType"), type);
    }
}
