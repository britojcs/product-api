package com.marketplace;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * A custom media type pattern enabler for spring hateoas
 * {@link TypeConstrainedMappingJackson2HttpMessageConverter}.
 * 
 * <p>
 * A wild card based media type application/*+json is set to support both versioning and HAL format
 * via Accept header, application/vnd.marketplace.v1+hal+json
 * 
 * @author Nikhil Vasaikar
 *
 */
@Component
public class VndHalMediaTypeEnabler {

  private static final MediaType VND_HAL = MediaType.valueOf("application/*+json");
  private final RequestMappingHandlerAdapter requestMappingHandlerAdapter;

  @Autowired
  VndHalMediaTypeEnabler(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
    this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
  }

  @PostConstruct
  public void enableVndHalJson() {
    for (HttpMessageConverter<?> converter : requestMappingHandlerAdapter.getMessageConverters()) {
      if (converter instanceof TypeConstrainedMappingJackson2HttpMessageConverter
          && converter.getSupportedMediaTypes().contains(MediaTypes.HAL_JSON)) {
        TypeConstrainedMappingJackson2HttpMessageConverter messageConverter =
            (TypeConstrainedMappingJackson2HttpMessageConverter) converter;
        messageConverter.setSupportedMediaTypes(Arrays.asList(VND_HAL, MediaTypes.HAL_JSON));
      }
    }
  }

}
