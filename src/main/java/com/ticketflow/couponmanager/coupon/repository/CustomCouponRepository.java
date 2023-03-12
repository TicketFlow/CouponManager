package com.ticketflow.couponmanager.coupon.repository;

import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomCouponRepository {

    Mono<Coupon> update(Coupon coupon);

    Flux<Coupon> findByFilter(CouponFilter couponFilter);

}
