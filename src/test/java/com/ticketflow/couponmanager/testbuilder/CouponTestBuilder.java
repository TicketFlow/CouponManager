package com.ticketflow.couponmanager.testbuilder;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.model.Coupon;

import java.time.LocalDateTime;

public class CouponTestBuilder {

    private static final String ID = "1";

    private static final String NAME = "Grouper test";

    private static final String DESCRIPTION = "Grouper description";

    private static final Float DISCOUNT_VALUE = 1F;

    private static final Float DISCOUNT_PERCENTAGE = 1F;

    private static final Status STATUS = Status.ACTIVE;

    private static final String CODE = "123ABC";
    private static final int USE_LIMIT = 10;

    private static final LocalDateTime EXPIRATION_DATE = LocalDateTime.now().plusDays(5).withSecond(0).withNano(0);

    public static CouponTestBuilder init() {
        return new CouponTestBuilder();
    }

    public Coupon.CouponBuilder buildModelWithDefaultValues() {
        return Coupon.builder()
                .id(ID)
                .name(NAME)
                .description(DESCRIPTION)
                .discountValue(DISCOUNT_VALUE)
                .discountPercentage(DISCOUNT_PERCENTAGE)
                .status(STATUS)
                .code(CODE)
                .expirationDate(EXPIRATION_DATE)
                .useLimit(USE_LIMIT);
    }

    public CouponDTO.CouponDTOBuilder buildDTOWithDefaultValues() {
        return CouponDTO.builder()
                .id(ID)
                .name(NAME)
                .description(DESCRIPTION)
                .discountValue(DISCOUNT_VALUE)
                .discountPercentage(DISCOUNT_PERCENTAGE)
                .status(STATUS)
                .code(CODE)
                .expirationDate(EXPIRATION_DATE)
                .useLimit(USE_LIMIT);
    }

    public CouponFilter.CouponFilterBuilder buildFilterWithDefaultValues() {
        return CouponFilter.builder()
                .id(ID)
                .name(NAME)
                .description(DESCRIPTION)
                .discountValue(DISCOUNT_VALUE)
                .discountPercentage(DISCOUNT_PERCENTAGE)
                .status(STATUS)
                .code(CODE)
                .expirationDate(EXPIRATION_DATE);
    }

}
