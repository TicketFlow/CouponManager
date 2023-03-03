package com.ticketflow.couponmanager.controller;

import com.ticketflow.couponmanager.model.Coupon;
import com.ticketflow.couponmanager.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping("/coupon")
@RestController
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping()
    public Flux<Coupon> getCoupons() {
        return couponService.getCoupons();
    }

}
