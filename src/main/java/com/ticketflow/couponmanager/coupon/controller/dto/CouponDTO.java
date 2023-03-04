package com.ticketflow.couponmanager.coupon.controller.dto;

import com.ticketflow.couponmanager.coupon.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private Float discountValue;

    private Float discountPercentage;

    private Status status;

    @NotNull
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime expirationDate;

    private String code;
}
