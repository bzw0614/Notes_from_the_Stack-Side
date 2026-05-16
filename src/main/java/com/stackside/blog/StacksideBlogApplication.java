package com.stackside.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StacksideBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(StacksideBlogApplication.class, args);
    }
}
