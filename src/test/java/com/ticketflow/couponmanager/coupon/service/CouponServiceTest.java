package com.ticketflow.couponmanager.coupon.service;


import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.exception.CouponException;
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
    public void createCoupon_WithValidCoupon_CreatesCoupon() {
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
    public void createCoupon_WithMissingDiscountFields_ThrowsCouponException() {
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_1");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_2");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_4");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_3");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_5");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_5");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_5");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_5");
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
                    assertThat(couponException.getErrorCode().getCode()).isEqualTo("CPM_SRVC_5");
                    return true;
                })
                .verify();


        verify(couponRepository, never()).save(any());
    }
}