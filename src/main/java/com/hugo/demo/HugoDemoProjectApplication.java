package com.hugo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class HugoDemoProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HugoDemoProjectApplication.class, args);
    }

}
