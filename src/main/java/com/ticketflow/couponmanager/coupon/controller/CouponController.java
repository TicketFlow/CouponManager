package com.ticketflow.couponmanager.coupon.controller;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.service.CouponService;
import org.apache.sshd.server.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
    public Mono<CouponDTO> createCoupon(@RequestBody @Validated CouponDTO coupon) {
        return couponService.createCoupon(coupon);
    }
//    @Value("${server.port}")
//    private String minhaProperty;
//
//    public void meuMetodo() {
//        System.out.println("Valor da property: " + minhaProperty);
//    }
//
//    @GetMapping("/test")
//    public String test() {
//        meuMetodo();
//        return minhaProperty;
//    }

}
