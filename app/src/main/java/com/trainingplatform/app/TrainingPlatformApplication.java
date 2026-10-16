package com.trainingplatform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.trainingplatform")
@EntityScan("com.trainingplatform")
@EnableJpaRepositories("com.trainingplatform")
public class TrainingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingPlatformApplication.class, args);
    }
}
