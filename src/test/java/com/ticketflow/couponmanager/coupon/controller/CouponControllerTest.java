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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


class CouponControllerTest {

    private final WebTestClient webTestClient;
    @Mock
    private CouponService couponService;
    @InjectMocks
    private CouponController couponController;

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
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(CouponDTO.class)
                .hasSize(1)
                .contains(coupon);

        verify(couponService).getCoupons(any(CouponFilter.class));
    }

    @Test
    @DisplayName("Get all coupons should return a list of coupons")
    public void getCoupons_withFilter_ReturnsListOfCoupons() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        CouponFilter couponFilter = CouponTestBuilder.init()
                .buildFilterWithDefaultValues()
                .build();

        when(couponService.getCoupons(any(CouponFilter.class))).thenReturn(Flux.fromIterable(List.of(coupon)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/coupon")
                        .queryParam("id", couponFilter.getId())
                        .queryParam("name", couponFilter.getName())
                        .queryParam("description", couponFilter.getDescription())
                        .queryParam("discountValue", couponFilter.getDiscountValue())
                        .queryParam("discountPercentage", couponFilter.getDiscountPercentage())
                        .queryParam("status", couponFilter.getStatus())
                        .queryParam("responsibleUser", couponFilter.getResponsibleUser())
                        .queryParam("expirationDate", couponFilter.getExpirationDate())
                        .queryParam("expirationDateStart", couponFilter.getExpirationDateStart())
                        .queryParam("expirationDateEnd", couponFilter.getExpirationDateEnd())
                        .queryParam("code", couponFilter.getCode())
                        .build())
                .accept(APPLICATION_JSON)
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
        when(couponService.createCoupon(coupon)).thenReturn(Mono.just(coupon));

        webTestClient.post()
                .uri("/coupon")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(coupon)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);

        verify(couponService).createCoupon(coupon);
    }

    @Test
    @DisplayName("Validating a coupon should return the validated coupon")
    public void validateCoupon_ReturnsValidatedCoupon() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponService.checkIfCouponIsValid(coupon.getId())).thenReturn(Mono.just(coupon));

        webTestClient.get()
                .uri("/coupon/{id}/validate", coupon.getId())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);

        verify(couponService).checkIfCouponIsValid(coupon.getId());
    }

    @Test
    @DisplayName("Should return updated coupon")
    public void updateCoupon_ReturnsUpdatedCoupon() {
        CouponDTO coupon = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponService.updateCoupon(coupon)).thenReturn(Mono.just(coupon));

        webTestClient.put()
                .uri("/coupon")
                .body(BodyInserters.fromValue(coupon))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(coupon);

        verify(couponService).updateCoupon(coupon);
    }

    @Test
    @DisplayName("Should deactivate coupon")
    void deactivateCoupon_returnsCoupon() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .status(Status.INACTIVE)
                .build();

        when(couponService.deactivateCoupon(couponDTO.getId())).thenReturn(Mono.just(couponDTO));

        webTestClient.put()
                .uri("/coupon/{id}/deactivate", couponDTO.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CouponDTO.class)
                .isEqualTo(couponDTO);
    }

}
