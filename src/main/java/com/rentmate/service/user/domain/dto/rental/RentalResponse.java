package com.rentmate.service.user.domain.dto.rental;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
public class RentalResponse {
    public static enum Status {
        Pending,
        Approved,
        Rejected,
        PaymentFailed,
        DeliveryFailed,
        Paid,
        InDelivery,
        Delivered,
        InReturning,
        Returned,
        Completed,
        LateReturning,
        Cancelled
    }

    private Long rentalId;
    private Long itemId;
    private  Long ownerId;
    private Long renterId;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private BigDecimal totalPrice;
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Date createdDate;
    private Date lastModifiedDate;
}