package com.ticketflow.couponmanager;

import com.ticketflow.couponmanager.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CouponmanagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(CouponmanagerApplication.class, args);
	}

}
