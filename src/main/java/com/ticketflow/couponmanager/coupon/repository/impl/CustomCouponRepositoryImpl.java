package com.ticketflow.couponmanager.coupon.repository.impl;

import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
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

import java.time.LocalDate;

@RequiredArgsConstructor
public class CustomCouponRepositoryImpl implements CustomCouponRepository {

    private static final String EXPIRATION_DATE_STRING = "expirationDate";
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Coupon> update(Coupon coupon) {
        if (coupon.getId() == null) {
            return Mono.error(new CouponException(CouponErrorCode.COUPON_ID_REQUIRED.withParams()));
        }

        Query query = new Query(Criteria.where("_id").is(coupon.getId()));

        Update update = new Update();

        if (coupon.getStatus() != null) {
            update.set("status", coupon.getStatus());
        }

        if (coupon.getExpirationDate() != null) {
            update.set(EXPIRATION_DATE_STRING, coupon.getExpirationDate());
        }

        if (coupon.getDescription() != null) {
            update.set("description", coupon.getDescription());
        }

        if (coupon.getDiscountValue() != null) {
            update.set("discountValue", coupon.getDiscountValue());
        }

        if (coupon.getDiscountPercentage() != null) {
            update.set("discountPercentage", coupon.getDiscountPercentage());
        }

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Coupon.class);
    }

    @Override
    public Mono<Coupon> updateUsage(Coupon coupon) {
        if (coupon.getId() == null) {
            return Mono.error(new CouponException(CouponErrorCode.COUPON_ID_REQUIRED.withParams()));
        }

        Query query = new Query(Criteria.where("_id").is(coupon.getId()));

        Update update = new Update();

        if (coupon.getUseLimit() != null) {
            update.set("useLimit", coupon.getUseLimit());
        }

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Coupon.class);
    }

    @Override
    public Mono<Coupon> findByCode(String code) {
        Query query = new Query(Criteria.where("code").is(code));
        return mongoTemplate.findOne(query, Coupon.class);
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

        if (couponFilter.getCode() != null) {
            query.addCriteria(Criteria.where("code").is(couponFilter.getCode()));
        }

        if (couponFilter.getExpirationDate() != null) {
            LocalDate expirationDate = couponFilter.getExpirationDate();
            query.addCriteria(Criteria.where(EXPIRATION_DATE_STRING).gte(expirationDate.atStartOfDay()).lt(expirationDate.plusDays(1).atStartOfDay()));
        }

        if (couponFilter.getExpirationDateStart() != null && couponFilter.getExpirationDateEnd() != null) {
            LocalDate startDate = couponFilter.getExpirationDateStart();
            LocalDate endDate = couponFilter.getExpirationDateEnd();
            query.addCriteria(Criteria.where(EXPIRATION_DATE_STRING).gte(startDate.atStartOfDay()).lt(endDate.plusDays(1).atStartOfDay()));
        } else if (couponFilter.getExpirationDateStart() != null) {
            LocalDate startDate = couponFilter.getExpirationDateStart();
            query.addCriteria(Criteria.where(EXPIRATION_DATE_STRING).gte(startDate.atStartOfDay()));
        } else if (couponFilter.getExpirationDateEnd() != null) {
            LocalDate endDate = couponFilter.getExpirationDateEnd();
            query.addCriteria(Criteria.where(EXPIRATION_DATE_STRING).lt(endDate.plusDays(1).atStartOfDay()));
        }


        return mongoTemplate.find(query, Coupon.class);
    }

    @Override
    public Mono<Coupon> updateApplicableCategories(Coupon coupon) {
        if (coupon.getId() == null) {
            return Mono.error(new CouponException(CouponErrorCode.COUPON_ID_REQUIRED.withParams()));
        }

        Query query = new Query(Criteria.where("_id").is(coupon.getId()));

        Update update = new Update();

        if (coupon.getApplicableCategories() != null) {
            update.set("applicableCategories", coupon.getApplicableCategories());
        }

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, Coupon.class);
    }

}
