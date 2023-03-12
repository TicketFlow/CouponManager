package com.ticketflow.couponmanager.coupon.service;


import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.ticketflow.couponmanager.coupon.controller.dto.CouponDTO;
import com.ticketflow.couponmanager.coupon.controller.filter.CouponFilter;
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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ModelMapper modelMapper = new ModelMapper();
        couponService = new CouponService(couponRepository, modelMapper);
    }

    @Test
    @DisplayName("Get coupons - return coupon list")
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

        when(couponRepository.findByFilter(new CouponFilter())).thenReturn(Flux.fromIterable(coupons));

        Flux<CouponDTO> result = couponService.getCoupons(new CouponFilter());

        StepVerifier.create(result)
                .expectNext(couponDTO1)
                .expectNext(couponDTO2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create coupon - when the coupon is valid, create a new coupon")
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
    @DisplayName("Create coupon - when try to create the coupon with no descount fields, return DISCOUNT_FIELD_MUST_BE_INFORMED")
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
    @DisplayName("Create coupon - when try to create coupon with expiration date less than actual date, returns EXPIRATION_DATE_LESS_THAN_CURRENT_DATE")
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
    @DisplayName("Create coupon - when try to creat coupon with percentage less than zero, return DISCOUNT_PERCENTAGE_LESS_THAN_ZERO")
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
    @DisplayName("Create coupon - when try to create coupon with discount value less then zero, return DISCOUNT_VALUE_LESS_THAN_ZERO")
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
    @DisplayName("Create coupon - when try to create coupon with empty name, return FIELD_CANNOT_BE_EMPTY")
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
    @DisplayName("Create coupon - when try to save coupon with name containing only spaces, return FIELD_CANNOT_BE_EMPTY")
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
    @DisplayName("Crate coupon - when try to create coupon with description empty, returns FIELD_CANNOT_BE_EMPTY")
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
    @DisplayName("Create coupon - when try to save description containing only spaces, return FIELD_CANNOT_BE_EMPTY")
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
    @DisplayName("create coupon - when try to save coupon with empty expiration date, return FIELD_CANNOT_BE_EMPTY")
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
    @DisplayName("validate coupon - when coupon doesn't exists, return COUPON_NOT_FOUND")
    void validateCoupon_couponNotFound_ReturnsError() {
        String COUPON_ID = "12345";
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
    @DisplayName("Validate coupon - when trying to validate a expired coupon, return COUPON_EXPIRED")
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
    @DisplayName("Validate coupon - when the coupon is disabled, returns INVALID_COUPON")
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
    @DisplayName("Validate coupon - Should validate a coupon")
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
    @DisplayName("Update coupon - when coupon exists and is valid, returns updated coupon")
    void updateCoupon_WhenCouponExistsAndIsValid_ReturnsUpdatedCoupon() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().status(Status.INACTIVE).build();
        Coupon coupon = CouponTestBuilder.init().buildModelWithDefaultValues().status(Status.INACTIVE).build();

        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(coupon));
        when(couponRepository.update(coupon)).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectNextMatches(updatedCoupon -> updatedCoupon.getId().equals(couponDTO.getId())
                        && updatedCoupon.getName().equals(couponDTO.getName())
                        && updatedCoupon.getDescription().equals(couponDTO.getDescription())
                        && updatedCoupon.getStatus().equals(couponDTO.getStatus())
                        && updatedCoupon.getDiscountValue().equals(couponDTO.getDiscountValue())
                        && updatedCoupon.getDiscountPercentage().equals(couponDTO.getDiscountPercentage())
                        && updatedCoupon.getCode().equals(couponDTO.getCode())
                        && updatedCoupon.getExpirationDate().equals(couponDTO.getExpirationDate()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Update coupon - when coupon does not exist, returns COUPON_NOT_FOUND")
    void updateCoupon_WhenCouponDoesNotExist_ReturnsCouponNotFoundException() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .build();

        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.empty());

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

         StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_NOT_FOUND.getCode());
                    return true;
                })
                .verify();
    }

    @Test
    @DisplayName("Update coupon - when status is empty, returns COUPON_STATUS_REQUIRED")
    void updateCoupon_WhenStatusIsEmpty_ReturnsCouponStatusRequiredException() {
        CouponDTO couponDTO = CouponTestBuilder.init()
                .buildDTOWithDefaultValues()
                .status(null)
                .build();

        Coupon coupon = CouponTestBuilder.init()
                .buildModelWithDefaultValues()
                .build();

        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_STATUS_REQUIRED.getCode());
                    return true;
                })
                .verify();
    }

    @Test
    @DisplayName("Update coupon - when coupon ID is null, returns COUPON_ID_REQUIRED")
    void updateCoupon_WhenCouponIdIsNull_ReturnsCouponIdRequiredException() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().id(null).build();

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_ID_REQUIRED.getCode());
                    return true;
                })
                .verify();
    }

    @Test
    @DisplayName("Update coupon - when coupon status is active and expiration date is null, returns COUPON_EXPIRATION_DATE_REQUIRED")
    void updateCoupon_WhenCouponStatusIsActiveAndExpirationDateIsNull_ReturnsCouponExpirationDateRequiredException() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().expirationDate(null).status(Status.ACTIVE).build();
        Coupon coupon = CouponTestBuilder.init().buildModelWithDefaultValues().expirationDate(null).status(Status.ACTIVE).build();

        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.COUPON_EXPIRATION_DATE_REQUIRED.getCode());
                    return true;
                })
                .verify();
    }

    @Test
    @DisplayName("Update coupon - when coupon status is active and expiration date is less than current date, returns EXPIRATION_DATE_LESS_THAN_CURRENT_DATE")
    void updateCoupon_WhenCouponStatusIsActiveAndExpirationDateIsLessThanCurrentDate_ReturnsCouponExpirationDateLessThanCurrentDateException() {
        CouponDTO couponDTO = CouponTestBuilder.init().buildDTOWithDefaultValues().status(Status.ACTIVE).expirationDate(LocalDateTime.now().minusDays(1)).build();
        Coupon coupon = CouponTestBuilder.init().buildModelWithDefaultValues().status(Status.ACTIVE).expirationDate(LocalDateTime.now().minusDays(1)).build();

        when(couponRepository.findById(couponDTO.getId())).thenReturn(Mono.just(coupon));

        Mono<CouponDTO> result = couponService.updateCoupon(couponDTO);

        StepVerifier.create(result)
                .expectErrorMatches(error -> {
                    assertThat(error).isInstanceOf(CouponException.class);
                    CouponException exception = (CouponException) error;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(CouponErrorCode.EXPIRATION_DATE_LESS_THAN_CURRENT_DATE.getCode());
                    return true;
                })
                .verify();
    }

}
