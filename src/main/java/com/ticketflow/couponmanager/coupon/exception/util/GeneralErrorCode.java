package com.ticketflow.couponmanager.coupon.exception.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode {

    INVALID_DATE_FORMAT("CPM_GNRL_ERR_1"),
    ILLEGAL_ARGUMENT_ERROR("CPM_GNRL_ERR_2"),
    BAD_REQUEST_ERROR("CPM_GNRL_ERR_3");

    private final String code;

    public ErrorCode withParams(Object... parameters) {
        return new ErrorCode(this.code, parameters);
    }

}
