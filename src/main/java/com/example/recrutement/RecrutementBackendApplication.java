package com.example.recrutement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.recrutement")  
@EnableAsync
@EnableScheduling
public class RecrutementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecrutementBackendApplication.class, args);
    }
}