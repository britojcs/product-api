package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import com.marketplace.security.JwtSecurityConfig;

@SpringBootApplication
@EnableEurekaClient
public class AuthServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }

  @Bean
  public JwtSecurityConfig jwtSecurityConfig() {
    return new JwtSecurityConfig();
  }

}
