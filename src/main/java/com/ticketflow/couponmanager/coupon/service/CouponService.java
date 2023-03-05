package com.ticketflow.couponmanager.coupon.service;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.NotFoundException;
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
import java.util.ArrayList;
import java.util.List;

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

    public Mono<CouponDTO> validateCoupon(String couponId) {
        log.info("Validate coupon. Id: " + couponId);

        return couponRepository.findById(couponId)
                .switchIfEmpty(Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponId))))
                .filter(coupon -> coupon.getExpirationDate().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new CouponException(CouponErrorCode.COUPON_EXPIRED.withParams(couponId))))
                .filter(coupon -> coupon.getStatus() == Status.ACTIVE)
                .switchIfEmpty(Mono.error(new CouponException(CouponErrorCode.INVALID_COUPON.withParams(couponId))))
                .map(coupon -> modelMapper.map(coupon, CouponDTO.class));
    }

    private Mono<CouponDTO> validateFields(CouponDTO coupon) {
        log.debug("Validating coupon");

        return checkForEmptyFields(coupon)
            .flatMap(couponDTO -> {
                boolean isDiscountValuePresent = couponDTO.getDiscountValue() != null;
                boolean isDiscountPercentagePresent = couponDTO.getDiscountPercentage() != null;
                boolean isExpirationDateValid = couponDTO.getExpirationDate().isAfter(LocalDateTime.now());
                boolean isDiscountValueValid = couponDTO.getDiscountValue() == null || couponDTO.getDiscountValue().compareTo(0f) > 0;
                boolean isDiscountPercentageValid = couponDTO.getDiscountPercentage() == null || couponDTO.getDiscountPercentage().compareTo(0f) > 0;

                if (!isDiscountValuePresent && !isDiscountPercentagePresent) {
                    return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_FIELD_MUST_BE_INFORMED.withNoParams()));
                }

                if (!isExpirationDateValid) {
                    return Mono.error(new CouponException(CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.withNoParams()));
                }

                if (!isDiscountPercentageValid) {
                    return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.withNoParams()));
                }

                if (!isDiscountValueValid) {
                    return Mono.error(new CouponException(CouponErrorCode.DISCOUNT_VALUE_LESS_THAN_ZERO.withNoParams()));
                }

                couponDTO.setStatus(Status.ACTIVE);
                return Mono.just(couponDTO);
            });
    }

    public Mono<CouponDTO> checkForEmptyFields(CouponDTO coupon) {
        List<String> emptyFields = new ArrayList<>();

        if (coupon.getName() == null || coupon.getName().isBlank()) {
            emptyFields.add("name");
        }
        if (coupon.getDescription() == null || coupon.getDescription().isBlank()) {
            emptyFields.add("description");
        }
        if (coupon.getExpirationDate() == null) {
            emptyFields.add("expirationDate");
        }

        if (!emptyFields.isEmpty()) {
            String fields = String.join(", ", emptyFields);
            return Mono.error(new CouponException(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams(fields)));
        }

        return Mono.just(coupon);
    }
}
