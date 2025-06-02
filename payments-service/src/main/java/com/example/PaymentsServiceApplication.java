package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // для аутбокс поллера
public class PaymentsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentsServiceApplication.class, args);
    }
}