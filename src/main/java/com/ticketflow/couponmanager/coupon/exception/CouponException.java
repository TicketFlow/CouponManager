package com.ticketflow.couponmanager.coupon.exception;

import com.ticketflow.couponmanager.coupon.exception.util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CouponException extends ConfigException {

    public CouponException(final ErrorCode errorCode) {
        super(errorCode);
    }

}
