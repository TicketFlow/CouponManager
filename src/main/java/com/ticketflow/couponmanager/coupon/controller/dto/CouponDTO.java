package com.ticketflow.couponmanager.coupon.controller.dto;

import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

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
