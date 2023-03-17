package com.ticketflow.couponmanager.coupon.controller.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime expirationDateStart;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime expirationDateEnd;

    private String code;
}