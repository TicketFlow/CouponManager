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
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> createCoupon(CouponDTO coupon) {
        log.info("Creating new coupon");

        return couponValidatorService.validateCreate(coupon)
                .flatMap(couponValidatorService::validateCouponCode)
                .doOnNext(CouponDTO::activate)
                .map(this::toCoupon)
                .flatMap(couponRepository::save)
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> updateCoupon(CouponDTO couponDTO) {
        log.info("Updating coupon id: {}", couponDTO.getId());

        return couponValidatorService.validateCouponId(couponDTO.getId())
                .then(findCouponById(couponDTO.getId()))
                .switchIfEmpty(Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponDTO.getId()))))
                .flatMap(couponEntity -> couponValidatorService.validateUpdate(couponDTO))
                .map(this::toCoupon)
                .flatMap(couponRepository::update)
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> validateAndDecreaseAvailableCoupons(String couponId) {
        return validateCoupon(couponId)
                .flatMap(couponDTO -> {
                    couponDTO.decrementUseLimit();
                    return updateCouponUsage(couponDTO);
                });
    }

    private Mono<CouponDTO> updateCouponUsage(CouponDTO couponDTO) {
        return Mono.just(couponDTO)
                .map(this::toCoupon)
                .flatMap(couponRepository::updateUsage)
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> validateCoupon(String couponId) {
        log.info("Validate coupon id: {}", couponId);

        return findCouponById(couponId)
                .flatMap(couponValidatorService::checkIfCouponIsExpired)
                .flatMap(couponValidatorService::checkIfCouponIsInactive)
                .flatMap(couponValidatorService::checkIfCouponHaveAvailableUses)
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> deactivateCoupon(String couponId) {
        return findCouponById(couponId)
                .flatMap(couponValidatorService::returnErrorIfCouponIsAlreadyInactive)
                .flatMap(this::deactivateAndSaveCoupon)
                .map(this::toCouponDTO);
    }

    public Mono<CouponDTO> addApplicableCategory(String couponId, String categoryId) {
        // todo - validar a categoria?
        log.info("Adding applicable category: {} to coupon {}", categoryId, couponId);

        return findCouponById(couponId)
                .flatMap(coupon -> validateAndAddApplicableCategory(coupon, categoryId))
                .flatMap(couponRepository::updateApplicableCategories)
                .map(this::toCouponDTO);
    }

    private Mono<Coupon> validateAndAddApplicableCategory(Coupon coupon, String categoryId) {
        return couponValidatorService.checkIfApplicableCategoryIsUnique(coupon, categoryId)
                .flatMap(validatedCoupon -> {
                    validatedCoupon.addApplicableCategory(categoryId);
                    return Mono.just(validatedCoupon);
                });
    }

    private Mono<Coupon> findCouponById(String couponId) {
        return couponRepository.findById(couponId)
                .switchIfEmpty(Mono.error(new NotFoundException(CouponErrorCode.COUPON_NOT_FOUND.withParams(couponId))));
    }

    private Mono<Coupon> deactivateAndSaveCoupon(Coupon coupon) {
        coupon.deactivate();
        return couponRepository.save(coupon);
    }

    private CouponDTO toCouponDTO(Coupon coupon) {
        return modelMapper.map(coupon, CouponDTO.class);
    }

    private Coupon toCoupon(CouponDTO couponDTO) {
        return modelMapper.map(couponDTO, Coupon.class);
    }
}
