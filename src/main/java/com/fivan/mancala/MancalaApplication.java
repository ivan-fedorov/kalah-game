package com.fivan.mancala;

import com.fivan.mancala.filter.AuthFilter;
import com.fivan.mancala.repository.PlayerRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class MancalaApplication {

  public static void main(String[] args) {
    SpringApplication.run(MancalaApplication.class, args);
  }

  @Configuration
  @EnableSwagger2
  public class SpringFoxConfig {
    @Bean
    public Docket api() {
      return new Docket(DocumentationType.SWAGGER_2)
          .select()
          .apis(RequestHandlerSelectors.any())
          .paths(PathSelectors.any())
          .build();
    }
  }

  @Bean
  public FilterRegistrationBean<AuthFilter> authFilterFilterRegistrationBean(PlayerRepository playerRepository) {
    FilterRegistrationBean<AuthFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new AuthFilter(playerRepository));
    return bean;
  }
}
