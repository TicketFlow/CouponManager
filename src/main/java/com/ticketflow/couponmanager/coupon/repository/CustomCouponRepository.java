package com.ticketflow.couponmanager.coupon.repository;

import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomCouponRepository {

    Mono<Coupon> updateStatus(String couponId, Status couponStatus);

    Flux<Coupon> findByFilter(CouponFilter couponFilter);

}
