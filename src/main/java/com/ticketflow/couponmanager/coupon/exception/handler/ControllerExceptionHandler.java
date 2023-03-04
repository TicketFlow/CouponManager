package com.ticketflow.couponmanager.coupon.exception.handler;


import com.ticketflow.couponmanager.coupon.exception.ConfigException;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
import com.ticketflow.couponmanager.coupon.exception.util.ErrorCode;
import com.ticketflow.couponmanager.coupon.exception.util.ErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    private final MessageSource messageSource;


    @ExceptionHandler(CouponException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage couponHandler(CouponException ex) {
        ErrorMessage error = new ErrorMessage(
                ex.getErrorCode().getCode(),
                messageSource.getMessage(ex.getErrorCode().getCode(), ex.getErrorCode().getParameters(), Locale.getDefault())
        );

        log.warn(ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(ConfigException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage genericHandler(ConfigException ex) {
        ErrorMessage error = new ErrorMessage(
                ex.getErrorCode().getCode(),
                messageSource.getMessage(ex.getErrorCode().getCode(), ex.getErrorCode().getParameters(), Locale.getDefault())
        );
        log.warn(ex.getMessage(), ex);
        return error;
    }
}