package com.ticketflow.couponmanager.coupon.service;


import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.enums.Status;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponService couponService;

    private ModelMapper modelMapper;

    private final String COUPON_ID = "12345";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        modelMapper = new ModelMapper();
        couponService = new CouponService(couponRepository, modelMapper);
    }

    @Test
    @DisplayName("Should return a coupon list")
    public void getCoupons_ReturnsAllCoupons() {
        Coupon coupon1 = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Coupon coupon2 = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .id("2")
                .name("Coupon 2")
                .description("Description 2")
                .build();

        CouponDTO couponDTO1 = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        CouponDTO couponDTO2 = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .id("2")
                .name("Coupon 2")
                .description("Description 2")
                .build();

        List<Coupon> coupons = Arrays.asList(coupon1, coupon2);

        when(couponRepository.findAll()).thenReturn(Flux.fromIterable(coupons));

        Flux<CouponDTO> result = couponService.getCoupons();

        StepVerifier.create(result)
                .expectNext(couponDTO1)
                .expectNext(couponDTO2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create a new coupon")
    public void createCoupon_WithValidCoupon_Succeed() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        Coupon savedCoupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .id("1")
                .status(Status.ACTIVE) // altera o status para ACTIVE
                .build();

        when(couponRepository.save(coupon)).thenReturn(Mono.just(savedCoupon));

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectNext(couponDTO)
                .verifyComplete();

        verify(couponRepository).save(coupon);
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with missing discount fields")
    public void createCoupon_WithMissingDiscountFields_ReturnsError() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(null)
                .discountValue(null)
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(coupon);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.DISCOUNT_FIELD_MUST_BE_INFORMED.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with expired date less than actual date")
    void createCoupon_ExpiredDate_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with percentage less than zero")
    void createCoupon_DiscountPercentageLessThanZero_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountPercentage(-1f)
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.DISCOUNT_PERCENTAGE_LESS_THAN_ZERO.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with value less than zero")
    void createCoupon_DiscountValueLessThanZero_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .discountValue(-1f)
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.DISCOUNT_VALUE_LESS_THAN_ZERO.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with empty name")
    void createCoupon_EmptyName_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .name("")
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with name containing only spaces")
    void createCoupon_NameWithSpaces_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .name("   ")
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with empty description")
    void createCoupon_WithEmptyDescription_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .description("")
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with description containing only spaces")
    void createCoupon_DescriptionWithSpaces_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .description("   ")
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to create a coupon with empty expiration date")
    void createCoupon_EmptyExpirationDate_ReturnsError() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .expirationDate(null)
                .build();

        Mono<CouponDTO> result = couponService.createCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException couponException = (CouponException) error;
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo(CouponErrorCode.FIELD_CANNOT_BE_EMPTY.getCode());
                    return true;
                })
                .verify();


        verify(couponRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return an error when trying to validate a coupon that doesn't exist")
    void validateCoupon_couponNotFound_ReturnsError() {
        when(couponRepository.findById(COUPON_ID)).thenReturn(Mono.empty());

        Mono<CouponDTO> result = couponService.validateCoupon(COUPON_ID);

        StepVerifier.create(result)
            .expectErrorMatches(error -> {
                assertThat(error).isInstanceOf(NotFoundException.class);
                NotFoundException exception = (NotFoundException) error;
                assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_NOT_FOUND.getCode());
                return true;
            })
            .verify();
    }

    @Test
    @DisplayName("Should return an error when trying to validate a coupon that is expired")
    void validateCoupon_couponExpired_ReturnsError() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.validateCoupon(coupon.getId());

        StepVerifier.create(result)
            .expectErrorMatches(error -> {
                assertThat(error).isInstanceOf(CouponException.class);
                CouponException exception = (CouponException) error;
                assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_EXPIRED.getCode());
                return true;
            })
            .verify();
    }

    @Test
    @DisplayName("Should return an error when trying to validate a coupon that is disabled")
    void validateCoupon_invalidCoupon_ReturnsError() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.validateCoupon(coupon.getId());

        StepVerifier.create(result)
            .expectErrorMatches(error -> {
                assertThat(error).isInstanceOf(CouponException.class);
                CouponException exception = (CouponException) error;
                assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.INVALID_COUPON.getCode());
                return true;
            })
            .verify();
    }

    @Test
    @DisplayName("Should validate a coupon")
    void validateCoupon_validCoupon_Succeed() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.validateCoupon(coupon.getId());

        StepVerifier.create(result)
                .expectNext(couponDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should deactivate a coupon")
    public void deactivateCoupon_validCoupon_Succeed() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));
        when(couponRepository.save(coupon)).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.deactivateCoupon(coupon.getId());

        StepVerifier.create(result)
                .expectNext(couponDTO)
                .verifyComplete();

        verify(couponRepository, times(1)).findById(coupon.getId());
        verify(couponRepository, times(1)).save(coupon);
    }

    @Test
    @DisplayName("Should return an error when trying to deactivate a coupon that is already inactive")
    public void deactivateCoupon_couponAlreadyInactive_ReturnsError() {
        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        when(couponRepository.findById(coupon.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.deactivateCoupon(coupon.getId());

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_ALREADY_INACTIVE.getCode());
                    return true;
                })
                .verify();

        verify(couponRepository, times(1)).findById(coupon.getId());
        verify(couponRepository, never()).save(any());
    }

}
