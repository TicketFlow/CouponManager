package com.ticketflow.couponmanager.repository;

import com.ticketflow.couponmanager.model.Coupon;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface  CouponRepository extends ReactiveMongoRepository<Coupon, String> {
}