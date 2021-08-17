package com.fivan.kalah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.ApiContextInitializer;

import java.util.ResourceBundle;

@SpringBootApplication
public class KalahApplication {

  public static void main(String[] args) {
    ApiContextInitializer.init();
    SpringApplication.run(KalahApplication.class, args);
  }

  @Bean
  public ResourceBundle messageBundle() {
    return ResourceBundle.getBundle("telegram_messages");
  }
}
