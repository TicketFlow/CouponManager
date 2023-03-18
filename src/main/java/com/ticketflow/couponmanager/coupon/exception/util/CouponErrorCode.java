package com.ticketflow.couponmanager.coupon.exception.util;

public enum CouponErrorCode {

    DISCOUNT_FIELD_MUST_BE_INFORMED("CPM_ERR_1"),
    EXPIRATION_DATE_LESS_THAN_CURRENT_DATE("CPM_ERR_2"),
    DISCOUNT_VALUE_LESS_THAN_ZERO("CPM_ERR_3"),
    DISCOUNT_PERCENTAGE_LESS_THAN_ZERO("CPM_ERR_4"),
    FIELD_CANNOT_BE_EMPTY("CPM_ERR_5"),
    COUPON_EXPIRED("CPM_ERR_6"),
    COUPON_NOT_FOUND("CPM_ERR_7"),
    INVALID_COUPON("CPM_ERR_8"),
    COUPON_ID_REQUIRED("CPM_ERR_9"),
    COUPON_ALREADY_INACTIVE("CPM_ERR_10"),
    COUPON_USAGE_LIMIT_REACHED("CPM_ERR_11"),
    COUPON_CODE_ALREADY_EXISTS("CPM_ERR_12");


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
