package com.fivan.kalah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class KalahApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(KalahApplication.class, args);
    }

}
