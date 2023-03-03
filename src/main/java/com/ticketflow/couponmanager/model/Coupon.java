package com.ticketflow.couponmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value="coupon")
public class Coupon {

    @Id
    private String id;

    private String name;

    private String description;

    private Float discountValue;

    private Float discountPercentage;

    private String active;

    private List<Long> type;

    private LocalDateTime expirationDate;

}
