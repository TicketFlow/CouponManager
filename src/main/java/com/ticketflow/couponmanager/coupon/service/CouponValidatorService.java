package com.ticketflow.couponmanager.coupon.service;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.NotFoundException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CouponValidatorService {

    public Mono<CouponDTO> validateCreate(CouponDTO coupon) {
        log.debug("Validating coupon id: {}", coupon.getId());

        return checkForEmptyFields(coupon)
                .flatMap(this::ensureExpirationDateIsNotInThePast)
                .flatMap(this::validateDiscountFields);
    }

    public Mono<Void> validateCouponId(String couponId) {
        if (couponId == null) {
            return Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponId)));
        }
        return Mono.empty();
    }

    public Mono<CouponDTO> validateUpdate(CouponDTO couponDTO) {
        return checkForEmptyFields(couponDTO)
                .flatMap(this::validateDiscountFields);
    }

    public Mono<Coupon> checkIfCouponIsExpired(Coupon coupon) {
        if (coupon.isExpired()) {
            return Mono.error(new CouponException(CouponErrorCode.COUPON_EXPIRED.withParams(coupon.getId())));
        }
        return Mono.just(coupon);
    }

    public Mono<Coupon> checkIfCouponIsInactive(Coupon coupon) {
        if (coupon.isInactive()) {
            return Mono.error(new CouponException(CouponErrorCode.INVALID_COUPON.withParams(coupon.getId())));
        }
        return Mono.just(coupon);
    }

    public Mono<Coupon> returnErrorIfCouponIsAlreadyInactive(Coupon coupon) {
        if (coupon.isInactive()) {
            return Mono.error(new CouponException(CouponErrorCode.COUPON_ALREADY_INACTIVE.withParams(coupon.getId())));
        }

        return Mono.just(coupon);
    }

    private Mono<CouponDTO> ensureExpirationDateIsNotInThePast(CouponDTO couponDTO) {
        if (couponDTO.getExpirationDate().compareTo(LocalDateTime.now()) <= 0) {
            return Mono.error(new CouponException(CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.withParams(couponDTO.getId())));
        }
        return Mono.just(couponDTO);
    }

    private Mono<CouponDTO> validateDiscountFields(CouponDTO couponDTO) {
        return checkIfDiscountFieldIsRequired(couponDTO)
                .flatMap(this::validateDiscountPercentage)
                .flatMap(this::validateDiscountValue);
    }

    private Mono<CouponDTO> checkIfDiscountFieldIsRequired(CouponDTO couponDTO) {
        if (couponDTO.getDiscountValue() == null && couponDTO.getDiscountPercentage() == null) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_FIELD_MUST_BE_INFORMED.withNoParams()));
        }
        return Mono.just(couponDTO);
    }

    private Mono<CouponDTO> validateDiscountPercentage(CouponDTO couponDTO) {
        if (couponDTO.getDiscountPercentage() != null && couponDTO.getDiscountPercentage().compareTo(0f) < 0) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.withNoParams()));
        }
        return Mono.just(couponDTO);
    }

    private Mono<CouponDTO> validateDiscountValue(CouponDTO couponDTO) {
        if (couponDTO.getDiscountValue() != null && couponDTO.getDiscountValue().compareTo(0f) < 0) {
            return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_VALUE_LESS_THAN_ZERO.withNoParams()));
        }
        return Mono.just(couponDTO);
    }

    private Mono<CouponDTO> checkForEmptyFields(CouponDTO coupon) {
        List<String> emptyFields = coupon.getEmptyFields();

        if (!emptyFields.isEmpty()) {
            String fields = String.join(", ", emptyFields);
            return Mono.error(new CouponException(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams(fields)));
        }

        return Mono.just(coupon);
    }

}
