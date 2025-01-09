package com.hugo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class HugoDemoProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HugoDemoProjectApplication.class, args);
    }

}
