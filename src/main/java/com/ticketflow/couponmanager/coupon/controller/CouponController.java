package com.ticketflow.couponmanager.coupon.controller;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.service.CouponService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/all")
    public Flux<CouponDTO> getCoupons() {
        return couponService.getCoupons();
    }

    @PostMapping
    public Mono<CouponDTO> createCoupon(@RequestBody CouponDTO coupon) {
        return couponService.createCoupon(coupon);
    }

    @GetMapping("/{id}/validate")
    public Mono<CouponDTO> validateCoupon(@PathVariable String id) {
        return couponService.validateCoupon(id);
    }

    @PutMapping("/{id}/deactivate")
    public Mono<CouponDTO> deactivateCoupon(@PathVariable String id) {
        return couponService.deactivateCoupon(id);
    }

}
