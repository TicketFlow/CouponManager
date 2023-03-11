package com.ticketflow.couponmanager.coupon.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {

    @Bean(name = "modelMapperConfig")
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
