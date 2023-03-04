package com.ticketflow.couponmanager.coupon.exception.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorMessage {

    private String code;

    private String message;

}
