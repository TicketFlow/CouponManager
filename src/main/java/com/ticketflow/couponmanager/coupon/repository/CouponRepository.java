package com.ticketflow.couponmanager.coupon.repository;

import com.ticketflow.couponmanager.coupon.model.Coupon;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface  CouponRepository extends ReactiveMongoRepository<Coupon, String>, CustomCouponRepository {
}