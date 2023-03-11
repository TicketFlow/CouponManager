package com.ticketflow.couponmanager.coupon.repository.impl;

import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import com.ticketflow.couponmanager.coupon.repository.CustomCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomCouponRepositoryImpl implements CustomCouponRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Coupon> updateStatus(String couponId, Status couponStatus) {
        Query query = new Query(Criteria.where("_id").is(couponId));
        Update update = new Update().set("status", couponStatus.name());

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Coupon.class);
    }

    @Override
    public Flux<Coupon> findByFilter(CouponFilter couponFilter) {
        Query query = new Query();

        if (couponFilter.getId() != null) {
            query.addCriteria(Criteria.where("_id").is(couponFilter.getId()));
        }

        if (couponFilter.getName() != null) {
            query.addCriteria(Criteria.where("name").is(couponFilter.getName()));
        }

        if (couponFilter.getDescription() != null) {
            query.addCriteria(Criteria.where("description").is(couponFilter.getDescription()));
        }

        if (couponFilter.getDiscountValue() != null) {
            query.addCriteria(Criteria.where("discountValue").is(couponFilter.getDiscountValue()));
        }

        if (couponFilter.getDiscountPercentage() != null) {
            query.addCriteria(Criteria.where("discountPercentage").is(couponFilter.getDiscountPercentage()));
        }

        if (couponFilter.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(couponFilter.getStatus()));
        }

        if (couponFilter.getResponsibleUser() != null) {
            query.addCriteria(Criteria.where("responsibleUser").is(couponFilter.getResponsibleUser()));
        }

        if (couponFilter.getExpirationDate() != null) {
            query.addCriteria(Criteria.where("expirationDate").is(couponFilter.getExpirationDate()));
        }

        if (couponFilter.getCode() != null) {
            query.addCriteria(Criteria.where("code").is(couponFilter.getCode()));
        }

        if (couponFilter.getExpirationDateBetween() != null && couponFilter.getExpirationDateBetween().getStart() != null && couponFilter.getExpirationDateBetween().getEnd() != null) {
            LocalDateTime startDate = couponFilter.getExpirationDateBetween().getStart();
            LocalDateTime endDate = couponFilter.getExpirationDateBetween().getEnd();

            query.addCriteria(Criteria.where("expirationDate").gte(startDate).lte(endDate));
        }

        return mongoTemplate.find(query, Coupon.class);
    }

}
