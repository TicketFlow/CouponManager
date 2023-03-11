package com.ticketflow.couponmanager.coupon.controller.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CouponFilter {

    private String id;

    private String name;

    private String description;

    private Float discountValue;

    private Float discountPercentage;

    private Status status;

    private String responsibleUser;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime expirationDate;

    private ExpirationDateRange expirationDateBetween;

    private String code;
}