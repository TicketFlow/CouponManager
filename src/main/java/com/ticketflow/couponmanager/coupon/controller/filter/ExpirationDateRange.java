package com.ticketflow.couponmanager.coupon.controller.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpirationDateRange {

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime start;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime end;

}
