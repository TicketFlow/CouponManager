package com.ticketflow.couponmanager.coupon.exception;

import com.ticketflow.couponmanager.coupon.exception.util.ErrorCode;
import org.apache.commons.lang.ArrayUtils;

public abstract class ConfigException extends RuntimeException {

    private static final String ERROR_CODE_NOT_FOUND = "Error code not found.";
    private final ErrorCode errorCode;

    public ConfigException(final ErrorCode error) {
        super(error != null ? error.code() : ERROR_CODE_NOT_FOUND);
        this.errorCode = error;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {

        StringBuilder suffix = new StringBuilder();
        if (errorCode != null && ArrayUtils.isNotEmpty(errorCode.parameters())) {
            suffix.append(" - ");
            for (Object parameter : errorCode.parameters()) {
                if (parameter == null)
                    suffix.append("null");
                else
                    suffix.append(parameter);
            }
        }

        return super.getMessage() + suffix;
    }

}
