package com.ticketflow.couponmanager.coupon.service;


import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
import com.ticketflow.couponmanager.coupon.exception.NotFoundException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import com.ticketflow.couponmanager.testbuilder.CouponTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

class CouponValidatorServiceTest {

    private CouponValidatorService couponValidatorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        couponValidatorService = new CouponValidatorService();
    }

    @Test
    public void validateCreate_ShouldReturnCouponDTO_WhenAllFieldsAreFilledAndExpirationDateIsValid() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenNameIsNull() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .name(null)
                .build();

        String errorMessage = CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams("name").code();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains("name"))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenNameIsEmpty() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().name("").build();
        String errorMessage = CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams("name").code();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains("name"))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenDescriptionIsNull() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().description(null).build();
        String errorMessage = CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams("description").code();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains("description"))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenDescriptionIsEmpty() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().description("").build();
        String errorMessage = CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams("description").code();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains("description"))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenExpirationDateIsNull() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().expirationDate(null).build();
        String errorMessage = CouponErrorCode.FIELD_CANNOT_BE_EMPTY.withParams("expirationDate").code();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains("expirationDate"))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_WhenDiscountValueAndDiscountPercentageAreNull() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountValue(null)
                .discountPercentage(null)
                .build();

        String errorMessage = CouponErrorCode.DISCOUNT_FIELD_MUST_BE_INFORMED.getCode();

        StepVerifier.create(couponValidatorService.validateCreate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().equals(errorMessage))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_whenExpirationDateIsLessThanCurrendDate() {
        LocalDateTime expirationDateInThePast = LocalDateTime.now().minusDays(1);

        CouponDTO invalidCouponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .expirationDate(expirationDateInThePast)
                .build();

        String errorMessage = CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.getCode();

        StepVerifier.create(couponValidatorService.validateCreate(invalidCouponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(invalidCouponDTO.getId()))
                .verify();
    }

    @Test
    public void validateCreate_ShouldThrowCouponException_whenCouponDiscountPercentageIsLessThanZero() {
        CouponDTO invalidCouponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(-1f)
                .build();

        String errorMessage = CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.getCode();

        StepVerifier.create(couponValidatorService.validateCreate(invalidCouponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage))
                .verify();
    }


    @Test
    public void validateCouponId_ShouldThrowCouponException_WhenCouponIdIsNull() {
        String errorMessage = CouponErrorCode.COUPON_NOT_FOUND.getCode();

        StepVerifier.create(couponValidatorService.validateCouponId(null))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains(errorMessage))
                .verify();
    }

    @Test
    public void validateCouponId_WhenIdIsValid_ValidatesCouponId() {
        String couponId = "1";

        Mono<Void> result = couponValidatorService.validateCouponId(couponId);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void validateUpdate_ShouldReturnCouponDTO_WhenDiscountPercentageIsZero() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(0F)
                .build();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    public void validateUpdate_ShouldThrowCouponException_WhenDiscountPercentageIsNegative() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(-1F)
                .build();

        String errorMessage = CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.getCode();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage))
                .verify();
    }

    @Test
    public void validateUpdate_ShouldThrowCouponException_WhenDiscountValueIsNegative() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountValue(-1F)
                .build();

        String errorMessage = CouponErrorCode.DISCOUNT_VALUE_LESS_THAN_ZERO.getCode();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage))
                .verify();
    }

    @Test
    public void validateUpdate_ShouldReturnCouponDTO_whenDiscountValueIsZero() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountValue(0F)
                .build();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    public void validateUpdate_ShouldReturnCouponDTO_WhenHaveOnlyDiscountPercentage() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountValue(null)
                .build();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    public void validateUpdate_ReturnCouponDTO_WhenDiscountPercentageIsNull() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(null)
                .build();

        StepVerifier.create(couponValidatorService.validateUpdate(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    void shouldReturnCouponWithNoChangesWhenExpirationDateIsInTheFuture() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Mono<Coupon> result = couponValidatorService.checkIfCouponIsExpired(coupon);

        StepVerifier.create(result)
                .expectNext(coupon)
                .verifyComplete();
    }

    @Test
    void givenValidCoupon_WhenExpirationDateIsInTheFuture_ReturnsCoupon() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Mono<Coupon> result = couponValidatorService.checkIfCouponIsExpired(coupon);

        StepVerifier.create(result)
                .expectNext(coupon)
                .verifyComplete();
    }

    @Test
    void givenInvalidCoupon_WithExpirationDateInThePast_shouldThrowCouponException() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .expirationDate(pastDate)
                .build();

        String errorMessage = CouponErrorCode.COUPON_EXPIRED.getCode();

        StepVerifier.create(couponValidatorService.checkIfCouponIsExpired(coupon))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(coupon.getId()))
                .verify();
    }

    @Test
    void givenInvalidCoupon_WithStatusExpired_shouldThrowCouponException() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.EXPIRED)
                .expirationDate(pastDate)
                .build();

        String errorMessage = CouponErrorCode.COUPON_EXPIRED.getCode();

        StepVerifier.create(couponValidatorService.checkIfCouponIsExpired(coupon))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(coupon.getId()))
                .verify();
    }

    @Test
    void givenInactiveCoupon_whenCheckIfCouponIsInactive_shouldReturnCouponException() {
        Coupon inactiveCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        String errorMessage = CouponErrorCode.INVALID_COUPON.getCode();

        StepVerifier.create(couponValidatorService.checkIfCouponIsInactive(inactiveCoupon))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(inactiveCoupon.getId()))
                .verify();
    }

    @Test
    void givenActiveCoupon_whenCheckIfCouponIsInactive_shouldReturnCoupon() {
        Coupon activeCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.ACTIVE)
                .build();

        StepVerifier.create(couponValidatorService.checkIfCouponIsInactive(activeCoupon))
                .expectNext(activeCoupon)
                .verifyComplete();
    }

    @Test
    void givenInactiveCoupon_whenReturnErrorIfCouponIsAlreadyInactive_shouldReturnCouponException() {
        Coupon inactiveCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        String errorMessage = CouponErrorCode.COUPON_ALREADY_INACTIVE.getCode();

        StepVerifier.create(couponValidatorService.returnErrorIfCouponIsAlreadyInactive(inactiveCoupon))
                .expectErrorMatches(throwable -> throwable instanceof CouponException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(inactiveCoupon.getId()))
                .verify();

    }

    @Test
    void givenActiveCoupon_whenReturnErrorIfCouponIsAlreadyInactive_shouldReturnCoupon() {
        Coupon activeCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        StepVerifier.create(couponValidatorService.returnErrorIfCouponIsAlreadyInactive(activeCoupon))
                .expectNext(activeCoupon)
                .verifyComplete();
    }

    @Test
    void checkIfCouponHaveAvailableUses_WhenCouponHasAvailableUses_ReturnsCoupon() {
        Coupon couponWithAvailableUses = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .useLimit(5)
                .build();

        Mono<Coupon> result = couponValidatorService.checkIfCouponHaveAvailableUses(couponWithAvailableUses);

        StepVerifier.create(result)
                .expectNext(couponWithAvailableUses)
                .expectComplete()
                .verify();
    }

    @Test
    void checkIfCouponHaveAvailableUses_WhenCouponHasNoAvailableUses_ThrowsCouponException() {
        Coupon couponWithNoAvailableUses = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .useLimit(0)
                .build();

        Mono<Coupon> result = couponValidatorService.checkIfCouponHaveAvailableUses(couponWithNoAvailableUses);

        String errorMessage = CouponErrorCode.COUPON_USAGE_LIMIT_REACHED.getCode();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CouponException &&
                        throwable.getMessage().contains(errorMessage) &&
                        throwable.getMessage().contains(couponWithNoAvailableUses.getId()))
                .verify();
    }

    @Test
    void checkIfCouponHaveAvailableUses_WhenUseLimitIsNull_ThrowsCouponException() {
        Coupon couponWithNoAvailableUses = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .useLimit(null)
                .build();

        Mono<Coupon> result = couponValidatorService.checkIfCouponHaveAvailableUses(couponWithNoAvailableUses);

        String errorMessage = CouponErrorCode.COUPON_USAGE_LIMIT_REACHED.getCode();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CouponException &&
                        throwable.getMessage().contains(errorMessage) &&
                        throwable.getMessage().contains(couponWithNoAvailableUses.getId()))
                .verify();
    }
}