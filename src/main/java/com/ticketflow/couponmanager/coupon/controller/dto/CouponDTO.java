package com.ticketflow.couponmanager.coupon.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponDTO {

    private String id;

    private String name;

    private String description;

    private Float discountValue;

    private Float discountPercentage;

    private Status status;

    private String responsibleUser;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime expirationDate;

    private String code;

    public void activate() {
        status = Status.ACTIVE;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @JsonIgnore
    public List<String> getEmptyFields() {
        List<String> emptyFields = new ArrayList<>();

        if (name == null || name.isBlank()) {
            emptyFields.add("name");
        }

        if (description == null || description.isBlank()) {
            emptyFields.add("description");
        }

        if (expirationDate == null) {
            emptyFields.add("expirationDate");
        }

        return emptyFields;
    }
}
