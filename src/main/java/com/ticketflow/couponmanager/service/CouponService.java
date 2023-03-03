package com.ticketflow.couponmanager.service;

import com.ticketflow.couponmanager.model.Coupon;
import com.ticketflow.couponmanager.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public Flux<Coupon> getCoupons() {
        return couponRepository.findAll();
    }
}
