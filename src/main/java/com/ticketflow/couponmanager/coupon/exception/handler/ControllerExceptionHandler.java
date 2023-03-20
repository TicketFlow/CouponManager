package com.ticketflow.couponmanager.coupon.exception.handler;


import com.ticketflow.couponmanager.coupon.exception.ConfigException;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.util.ErrorMessage;
import com.ticketflow.couponmanager.coupon.exception.util.GeneralErrorCode;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    private final MessageSource messageSource;


    @ExceptionHandler(CouponException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage couponHandler(CouponException ex) {
        ErrorMessage error = new ErrorMessage(
                ex.getErrorCode().code(),
                messageSource.getMessage(ex.getErrorCode().code(), ex.getErrorCode().parameters(), Locale.getDefault())
        );

        log.warn(ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(ConfigException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage genericHandler(ConfigException ex) {
        ErrorMessage error = new ErrorMessage(
                ex.getErrorCode().code(),
                messageSource.getMessage(ex.getErrorCode().code(), ex.getErrorCode().parameters(), Locale.getDefault())
        );
        log.warn(ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleDateTimeParseException(DateTimeParseException ex) {
        ErrorMessage error = new ErrorMessage(
                GeneralErrorCode.INVALID_DATE_FORMAT.getCode(),
                messageSource.getMessage(GeneralErrorCode.INVALID_DATE_FORMAT.getCode(), null, Locale.getDefault())
        );
        log.warn(ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage badRequestHandler(BadRequestException ex) {
        ErrorMessage error = new ErrorMessage(
                GeneralErrorCode.BAD_REQUEST_ERROR.getCode(),
                messageSource.getMessage("BAD_REQUEST_ERROR", null, Locale.getDefault())
        );
        log.warn(ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage illegalArgumentHandler(IllegalArgumentException ex) {
        ErrorMessage error = new ErrorMessage(
                GeneralErrorCode.ILLEGAL_ARGUMENT_ERROR.getCode(),
                messageSource.getMessage(GeneralErrorCode.ILLEGAL_ARGUMENT_ERROR.getCode(), null, Locale.getDefault())
        );
        log.warn(ex.getMessage(), ex);
        return error;
    }

}