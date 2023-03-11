package com.ticketflow.couponmanager.coupon.controller;

import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
import com.ticketflow.couponmanager.coupon.enums.Status;
import com.ticketflow.couponmanager.coupon.service.CouponService;
import com.ticketflow.couponmanager.testbuilder.CouponTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class CouponControllerTest {

    @Mock
    private CouponService couponService;

    @InjectMocks
    private  CouponController couponController;

    private final WebTestClient webTestClient;

    public CouponControllerTest() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(couponController).build();
    }

    @BeforeEach
    public void setUp() {
        this.couponController = new CouponController(couponService);
    }

    @Test
    @DisplayName("Get all coupons should return a list of coupons")
    public void getCoupons_ReturnsListOfCoupons() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponService.getCoupons(any(CouponFilter.class))).thenReturn(Flux.fromIterable(List.of(coupon)));

        webTestClient.get()
                .uri("/coupon")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(CouponDTO.class)
                .hasSize(1)
                .contains(coupon);
    }

    @Test
    @DisplayName("Creating a coupon should return the created coupon")
    public void createCoupon_ReturnsCreatedCoupon() {
        CouponDTO coupon = CouponTestBuilder.init().buildDTOWithDefaultValues().build();
        when(couponService.createCoupon(any())).thenReturn(Mono.just(coupon));

        webTestClient.post()
                .uri("/coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(coupon)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);
    }

    @Test
    @DisplayName("Validating a coupon should return the validated coupon")
    public void validateCoupon_ReturnsValidatedCoupon() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponService.validateCoupon(any())).thenReturn(Mono.just(coupon));

        webTestClient.get()
                .uri("/coupon/{id}/validate", "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);
    }

    @Test
    @DisplayName("Deactivating an active coupon should return the deactivated coupon")
    public void deactivateCoupon_ReturnsDeactivatedCoupon() {
        CouponDTO coupon = CouponTestBuilder.init().buildDTOWithDefaultValues().status(Status.ACTIVE).build();
        when(couponService.deactivateCoupon(any())).thenReturn(Mono.just(coupon));

        webTestClient.put()
                .uri("/coupon/{id}/deactivate", "1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);
    }
}
