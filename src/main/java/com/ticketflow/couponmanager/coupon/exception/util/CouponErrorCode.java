package com.ticketflow.couponmanager.coupon.exception.util;

public enum CouponErrorCode {

    DISCOUNT_FIELD_MUST_BE_INFORMED("CPM_SRVC_1"),
    EXPIRATION_DATE_LESS_THAN_CURRENT_DATE("CPM_SRVC_2"),
    DISCOUNT_VALUE_LESS_THAN_ZERO("CPM_SRVC_3"),
    DISCOUNT_PERCENTAGE_LESS_THAN_ZERO("CPM_SRVC_4"),
    FIELD_CANNOT_BE_EMPTY("CPM_SRVC_5"),
    COUPON_EXPIRED("CPM_SRVC_6"),
    COUPON_NOT_FOUND("CPM_SRVC_7"),
    INVALID_COUPON("CPM_SRVC_8");


    private final String code;

    CouponErrorCode(final String code) {
        this.code = code;
    }

    public ErrorCode withNoParams() {
        return withParams();
    }

    public ErrorCode withParams(Object... parameters) {
        return new ErrorCode(this.code, parameters);
    }

    public String getCode() {
        return code;
    }
}
