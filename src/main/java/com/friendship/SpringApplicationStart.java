package com.friendship;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringApplicationStart {
    public static void main(String[] args) {
        SpringApplication.run(SpringApplicationStart.class, args);
    }
}
