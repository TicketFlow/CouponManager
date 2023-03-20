package com.ticketflow.couponmanager.coupon.model;

import com.ticketflow.couponmanager.coupon.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "coupon")
public class Coupon {

    @Id
    private String id;

    private String name;

    private String description;

    private Float discountValue;

    private Float discountPercentage;

    private Status status;

    private String code;

    private LocalDateTime expirationDate;

    private String responsibleUser;

    private Integer useLimit;

    private List<String> applicableCategories;

    public void deactivate() {
        status = Status.INACTIVE;
    }

    public boolean isInactive() {
        return status == Status.INACTIVE;
    }

    public boolean isExpired() {
        return status == Status.EXPIRED || expirationDate.isBefore(LocalDateTime.now());
    }

    public void addApplicableCategory(String category) {
        if (applicableCategories == null) {
            applicableCategories = new ArrayList<>();
        }

        applicableCategories.add(category);
    }

    public void removeApplicableCategory(String category) {
        if (applicableCategories != null) {
            applicableCategories.remove(category);
        }
    }

    public boolean hasAvailableUses() {
        if (useLimit == null) {
            return false;
        }
        return useLimit > 0;
    }

}
