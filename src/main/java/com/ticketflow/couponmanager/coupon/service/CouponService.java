package com.ticketflow.couponmanager.coupon.service;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
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

@Slf4j
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    private final CouponValidatorService couponValidatorService;

    @Qualifier("modelMapperConfig")
    private final ModelMapper modelMapper;

    public CouponService(CouponRepository couponRepository, CouponValidatorService couponValidatorService, ModelMapper modelMapper) {
        this.couponRepository = couponRepository;
        this.couponValidatorService = couponValidatorService;
        this.modelMapper = modelMapper;
    }

    public Flux<CouponDTO> getCoupons(CouponFilter couponFilter) {
        log.info("Getting coupons");

        return couponRepository.findByFilter(couponFilter)
                .map(coupon -> modelMapper.map(coupon, CouponDTO.class));
    }

    public Mono<CouponDTO> createCoupon(CouponDTO coupon) {
        log.info("Creating new coupon");

        return couponValidatorService.validateCreate(coupon)
                .doOnNext(CouponDTO::activate)
                .map(couponDTO -> modelMapper.map(couponDTO, Coupon.class))
                .flatMap(couponRepository::save)
                .map(couponEntity -> modelMapper.map(couponEntity, CouponDTO.class));
    }

    public Mono<CouponDTO> updateCoupon(CouponDTO couponDTO) {
        log.info("Updating coupon id: {}", couponDTO.getId());

        return couponValidatorService.validateCouponId(couponDTO.getId())
                .then(couponRepository.findById(couponDTO.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponDTO.getId()))))
                        .flatMap(couponEntity -> couponValidatorService.validateUpdate(couponDTO)
                                .map(coupon -> modelMapper.map(coupon, Coupon.class))
                                .flatMap(couponRepository::update)
                                .map(updatedCoupon -> modelMapper.map(updatedCoupon, CouponDTO.class))));
    }

    public Mono<CouponDTO> checkIfCouponIsValid(String couponId) {
        log.info("Validate coupon id: {}", couponId);

        return couponRepository.findById(couponId)
                .switchIfEmpty(Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponId))))
                .flatMap(couponValidatorService::checkIfCouponIsExpired)
                .flatMap(couponValidatorService::checkIfCouponIsInactive)
                .map(coupon -> modelMapper.map(coupon, CouponDTO.class));
    }

    public Mono<CouponDTO> deactivateCoupon(String couponId) {
        return couponRepository.findById(couponId)
                .flatMap(couponValidatorService::returnErrorIfCouponIsAlreadyInactive)
                .flatMap(this::deactivateAndSaveCoupon)
                .map(savedCoupon -> modelMapper.map(savedCoupon, CouponDTO.class));
    }

    private Mono<Coupon> deactivateAndSaveCoupon(Coupon coupon) {
        coupon.deactivate();
        return couponRepository.save(coupon);
    }
}
