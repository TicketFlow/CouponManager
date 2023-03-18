package com.ticketflow.couponmanager.coupon.service;


import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.exception.NotFoundException;
import com.ticketflow.couponmanager.coupon.exception.util.CouponErrorCode;
import com.ticketflow.couponmanager.coupon.model.Coupon;
import com.ticketflow.couponmanager.coupon.repository.CouponRepository;
import com.ticketflow.couponmanager.testbuilder.CouponTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponValidatorService couponValidatorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ModelMapper modelMapper = new ModelMapper();
        couponService = new CouponService(couponRepository, couponValidatorService, modelMapper);
    }

    @Test
    @DisplayName("Get coupons - return all coupons")
    void getCoupons_ReturnsAllCoupons() {
        Coupon coupon1 = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .id("1")
                .build();

        Coupon coupon2 = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .id("2")
                .build();

        CouponFilter filter = new CouponFilter();

        when(couponRepository.findByFilter(filter)).thenReturn(Flux.just(coupon1, coupon2));

        StepVerifier.create(couponService.getCoupons(filter))
                .expectNext(CouponTestBuilder.init().buildDTOWithDefaultValues().id("1").build())
                .expectNext(CouponTestBuilder.init().buildDTOWithDefaultValues().id("2").build())
                .verifyComplete();

        verify(couponRepository).findByFilter(filter);

    }


    @Test
    @DisplayName("Create coupon - when coupon is valid, create new coupon")
    public void createCoupon_WhenCouponIsValid_CreateCoupon() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        when(couponValidatorService.validateCreate(couponDTO)).thenReturn(Mono.just(couponDTO));
        when(couponValidatorService.validateCouponCode(couponDTO)).thenReturn(Mono.just(couponDTO));
        when(couponRepository.save(coupon)).thenReturn(Mono.just(coupon));

        StepVerifier.create(couponService.createCoupon(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();

        verify(couponValidatorService, times(1)).validateCreate(couponDTO);
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    @DisplayName("Update coupon - when coupon is found and valid, updates coupon")
    void updateCoupon_WhenCouponIsFoundAndValid_UpdatesCoupon() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        Coupon couponToUpdate = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Coupon updatedCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        when(couponValidatorService.validateCouponId(couponDTO.getId())).thenReturn(Mono.empty());
        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(couponToUpdate));
        when(couponValidatorService.validateUpdate(couponDTO)).thenReturn(Mono.just(couponDTO));
        when(couponRepository.update(any(Coupon.class))).thenReturn(Mono.just(updatedCoupon));

        StepVerifier.create(couponService.updateCoupon(couponDTO))
                .expectNext(couponDTO)
                .verifyComplete();

        verify(couponValidatorService, times(1)).validateCouponId(couponDTO.getId());
        verify(couponValidatorService, times(1)).validateUpdate(couponDTO);
        verify(couponRepository, times(1)).findById(couponDTO.getId());
        verify(couponRepository, times(1)).update(any(Coupon.class));
    }

    @Test
    @DisplayName("Given a couponDTO with invalid id, when updateCoupon is called, then it should throw CouponException with COUPON_NOT_FOUND error code")
    void givenInvalidId_whenUpdateCoupon_thenThrowCouponExceptionWithCouponNotFoundErrorCode() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponValidatorService.validateCouponId(couponDTO.getId())).thenReturn(Mono.empty());
        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.empty());

        String errorMessage = CouponErrorCode.COUPON_NOT_FOUND.getCode();

        StepVerifier.create(couponService.updateCoupon(couponDTO))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains(errorMessage)
                        && throwable.getMessage().contains(couponDTO.getId()))
                .verify();

        verify(couponRepository, never()).update(any());
    }

    @Test
    @DisplayName("Check if coupon is valid - when coupon is valid, return coupon")
    void checkIfCouponIsValid_WhenCouponIsValid_ReturnCoupon() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        CouponDTO expectedCouponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsExpired(coupon)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsInactive(coupon)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponHaveAvailableUses(coupon)).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> actualCouponDTO = couponService.validateCoupon(coupon.getId());

        StepVerifier.create(actualCouponDTO)
                .expectNext(expectedCouponDTO)
                .expectComplete()
                .verify();

        verify(couponValidatorService, times(1)).checkIfCouponIsExpired(coupon);
        verify(couponValidatorService, times(1)).checkIfCouponIsInactive(coupon);

    }

    @Test
    @DisplayName("Check if coupon is valid - when coupon is not found, returns COUPON_NOT_FOUND")
    void checkIfCouponIsValid_WhenCouponIsNotFound_ReturnsCouponNotFoundException() {
        String couponId = "invalid-id";

        when(couponRepository.findById(couponId)).thenReturn(Mono.empty());

        String errorMessage = CouponErrorCode.COUPON_NOT_FOUND.getCode();

        StepVerifier.create(couponService.validateCoupon(couponId))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains(errorMessage))
                .verify();

        verify(couponValidatorService, never()).checkIfCouponIsExpired(any());
        verify(couponValidatorService, never()).checkIfCouponIsInactive(any());
    }


    @Test
    void deactivateCoupon_WhenCouponIsActive_ReturnsCoupon() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Coupon inactiveCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        when(couponRepository.findById(anyString())).thenReturn(Mono.just(coupon));
        when(couponValidatorService.returnErrorIfCouponIsAlreadyInactive(any(Coupon.class))).thenReturn(Mono.just(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(Mono.just(inactiveCoupon));

        StepVerifier.create(couponService.deactivateCoupon(coupon.getId()))
                .assertNext(couponDTO -> {
                    assertEquals(coupon.getId(), couponDTO.getId());
                    assertFalse(couponDTO.isActive());
                })
                .verifyComplete();

        verify(couponRepository).findById(coupon.getId());
        verify(couponValidatorService).returnErrorIfCouponIsAlreadyInactive(coupon);
        verify(couponRepository).save(inactiveCoupon);
    }

    @Test
    void validateAndDecreaseAvailableCoupons_WhenCouponIsValid_UpdatesCouponUsage() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .useLimit(5)
                .build();

        CouponDTO expectedCouponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .useLimit(4)
                .build();

        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .useLimit(5)
                .build();

        Coupon couponUpdated = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .useLimit(4)
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsExpired(coupon)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsInactive(coupon)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponHaveAvailableUses(coupon)).thenReturn(Mono.just(coupon));
        when(couponRepository.updateUsage(any(Coupon.class))).thenReturn(Mono.just(couponUpdated));

        StepVerifier.create(couponService.validateAndDecreaseAvailableCoupons(couponDTO.getId()))
                .expectNext(expectedCouponDTO)
                .expectComplete()
                .verify();

        verify(couponValidatorService, times(1)).checkIfCouponIsExpired(coupon);
        verify(couponValidatorService, times(1)).checkIfCouponIsInactive(coupon);
        verify(couponValidatorService, times(1)).checkIfCouponHaveAvailableUses(coupon);
    }
}
