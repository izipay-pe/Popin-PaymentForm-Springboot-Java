package com.example.popinpaymentform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PopinPaymentFormApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PopinPaymentFormApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PopinPaymentFormApplication.class, args);
    }
}
