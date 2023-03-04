package com.ticketflow.couponmanager.coupon.controller;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequestMapping("/coupon")
@RestController
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping()
    public Flux<CouponDTO> getCoupons() {
        return couponService.getCoupons();
    }

    @PostMapping
    public Mono<CouponDTO> createCoupon(@RequestBody CouponDTO coupon) {
        return couponService.createCoupon(coupon);
    }

}
