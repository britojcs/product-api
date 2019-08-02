package com.marketplace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

/**
 * 
 * A configurer to set HAL media type as default
 * 
 * @author Nikhil Vasaikar
 *
 */
@Configuration
public class DefaultMediaTypeConfiguration implements RepositoryRestConfigurer {

  @Value("${spring.data.rest.defaultMediaType:true}")
  private boolean useHalAsDefaultJsonType;

  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {

    config.useHalAsDefaultJsonMediaType(useHalAsDefaultJsonType);
  }
}
