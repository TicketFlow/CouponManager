package com.ticketflow.couponmanager.coupon.service;


import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CouponServiceTest {

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

        Flux<CouponDTO> result = couponService.getCoupons(filter);

        StepVerifier.create(result)
                .expectNext(CouponTestBuilder.init().buildDTOWithDefaultValues().id("1").build())
                .expectNext(CouponTestBuilder.init().buildDTOWithDefaultValues().id("2").build())
                .verifyComplete();

        verify(couponRepository).findByFilter(filter);
    }


    @Test
    @DisplayName("Create coupon - when coupon is valid, create new coupon")
    void createCoupon_WhenCouponIsValid_CreateCoupon() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        when(couponValidatorService.validateCreate(couponDTO)).thenReturn(Mono.just(couponDTO));
        when(couponRepository.save(coupon)).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
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

        Coupon updatedCoupon = CouponTestBuilder.init().buildModelWithDefaultValues().build();

        when(couponValidatorService.validateCouponId(couponDTO.getId())).thenReturn(Mono.empty());
        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(couponToUpdate));
        when(couponValidatorService.validateUpdate(couponDTO)).thenReturn(Mono.just(couponDTO));
        when(couponRepository.update(any(Coupon.class))).thenReturn(Mono.just(updatedCoupon));

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectNext(couponDTO)
                .verifyComplete();

        verify(couponValidatorService, times(1)).validateCouponId(couponDTO.getId());
        verify(couponValidatorService, times(1)).validateUpdate(couponDTO);
        verify(couponRepository, times(1)).findById(couponDTO.getId());
        verify(couponRepository, times(1)).update(any(Coupon.class));
    }

    @Test
    @DisplayName("Given a couponDTO with invalid id, when updateCoupon is called, then it should throw CouponException with COUPON_NOT_FOUND error code")
    public void givenInvalidId_whenUpdateCoupon_thenThrowCouponExceptionWithCouponNotFoundErrorCode() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().build();

        when(couponValidatorService.validateCouponId(couponDTO.getId())).thenReturn(Mono.empty());
        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.empty());

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);


        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().code()).isEqualTo(CouponErrorCode.COUPON_NOT_FOUND.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).update(any());
    }

    @Test
    @DisplayName("Check if coupon is valid - when coupon is valid, return coupon")
    void checkIfCouponIsValid_WhenCouponIsValid_ReturnCoupon() {
        String couponId = "1";
        Coupon coupon = CouponTestBuilder.init().buildModelWithDefaultValues().id(couponId).build();
        CouponDTO expectedCouponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().id(couponId).build();

        when(couponRepository.findById(couponId)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsExpired(coupon)).thenReturn(Mono.just(coupon));
        when(couponValidatorService.checkIfCouponIsInactive(coupon)).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> actualCouponDTO = couponService.checkIfCouponIsValid(couponId);

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

        Mono<CouponDTO> result = couponService.checkIfCouponIsValid(couponId);


        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(NotFoundException.class);
                    NotFoundException exception = (NotFoundException) error;
                    assertThat(exception.getErrorCode().code()).isEqualTo(CouponErrorCode.COUPON_NOT_FOUND.getCode());
                    return true;
                })
                .verify();

        verify(couponValidatorService, never()).checkIfCouponIsExpired(any());
        verify(couponValidatorService, never()).checkIfCouponIsInactive(any());

    }

}
