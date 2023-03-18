package com.ticketflow.couponmanager.coupon.controller.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.*;

import java.time.LocalDate;

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

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate expirationDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate expirationDateStart;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate expirationDateEnd;

    private String code;
}