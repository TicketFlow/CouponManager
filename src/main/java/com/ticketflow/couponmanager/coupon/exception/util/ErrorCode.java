package com.ticketflow.couponmanager.coupon.exception.util;

import java.io.Serializable;

public class ErrorCode implements Serializable {

    private final String code;
    private final transient Object[] parameters;

    public ErrorCode(final String code, final Object[] parameters) {
        this.code = code;
        this.parameters = parameters;
    }

    public String getCode() {
        return code;
    }

    public Object[] getParameters() {
        return parameters;
    }
}