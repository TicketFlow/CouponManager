package com.ticketflow.couponmanager.coupon.exception;

import com.ticketflow.couponmanager.coupon.exception.util.ErrorCode;
import org.apache.commons.lang.ArrayUtils;

public abstract class ConfigException extends RuntimeException {

    private final ErrorCode errorCode;

    private static final String ERROR_CODE_NOT_FOUND  = "Error code not found.";

    public ConfigException(final ErrorCode error) {
        super(error != null ? error.getCode() : ERROR_CODE_NOT_FOUND);
        this.errorCode = error;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {

        String suffix = "";
        if (errorCode != null && ArrayUtils.isNotEmpty(errorCode.getParameters())) {
            suffix += " - ";
            for (Object parameter : errorCode.getParameters()) {
                if (parameter == null)
                    suffix += "null";
                else
                    suffix += parameter.toString();
            }
        }

        return super.getMessage() + suffix;
    }

}
