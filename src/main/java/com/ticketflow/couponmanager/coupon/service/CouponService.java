package com.ticketflow.couponmanager.coupon.service;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import com.ticketflow.couponmanager.coupon.repository.CouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    @Qualifier("modelMapperConfig")
    private final ModelMapper modelMapper;

    public CouponService(CouponRepository couponRepository, ModelMapper modelMapper) {
        this.couponRepository = couponRepository;
        this.modelMapper = modelMapper;
    }

    public Flux<CouponDTO> getCoupons() {
        log.info("Getting all coupons");
        return couponRepository.findAll()
                .map(coupon -> modelMapper.map(coupon, CouponDTO.class));
    }

    public Mono<CouponDTO> createCoupon(CouponDTO coupon) {
        log.info("Creating new coupon");
        return validateFields(coupon)
                .map(couponDTO -> modelMapper.map(couponDTO, Coupon.class))
                .flatMap(couponRepository::save)
                .map(couponEntity -> modelMapper.map(couponEntity, CouponDTO.class));
    }

    private Mono<CouponDTO> validateFields(CouponDTO coupon) {
        log.debug("Validating coupon");

        boolean isDiscountValuePresent = coupon.getDiscountValue() != null;
        boolean isDiscountPercentagePresent = coupon.getDiscountPercentage() != null;
        boolean isExpirationDateValid = coupon.getExpirationDate().isAfter(LocalDateTime.now());
        boolean isDiscountValueValid = coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(0f) > 0;
        boolean isDiscountPercentageValid = coupon.getDiscountPercentage() == null || coupon.getDiscountPercentage().compareTo(0f) > 0;

        if (!isDiscountValuePresent && !isDiscountPercentagePresent) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_FIELD_MUST_BE_INFORMED.withNoParams()));
        }

        if (!isExpirationDateValid) {
            return Mono.error(new CouponException(CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.withNoParams()));
        }

        if (!isDiscountPercentageValid) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.withNoParams()));
        }

        if (!isDiscountValueValid ) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_VALUE_LESS_THAN_ZERO.withNoParams()));
        }

        coupon.setStatus(Status.ACTIVE);

        return Mono.just(coupon);
    }
}
