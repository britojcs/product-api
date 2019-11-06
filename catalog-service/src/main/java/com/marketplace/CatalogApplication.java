package com.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.filter.ForwardedHeaderFilter;

@SpringBootApplication
@EnableEurekaClient
public class CatalogApplication {

  public static void main(String[] args) {
    SpringApplication.run(CatalogApplication.class, args);
  }

  /**
   * This Bean is required to enable x-forwarded-host & x-forwarded-prefix headers so that
   * {@link ControllerLinkBuilder} in spring-hateoas can generate zuul gateway links for hateoas
   * resources instead of direct service links. Spring boot disables these headers by default
   * 
   * @return
   */
  @Bean
  ForwardedHeaderFilter forwardedHeaderFilter() {
    return new ForwardedHeaderFilter();
  }
}
