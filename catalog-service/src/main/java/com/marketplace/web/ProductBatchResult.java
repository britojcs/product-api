package com.marketplace.web;

import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * A {@link Product} wrapper used for batch insertion of products.
 * 
 * @author Nikhil Vasaikar
 *
 * @param <T> {@link Product} or {@link ProductResource}
 * 
 * @see {@link ProductController#newProducts(ProductBatch)},
 *      {@link ProductHalController#newProducts(ProductBatch)}
 */
@JsonInclude(Include.NON_NULL)
public class ProductBatchResult<T> {

  private HttpStatus httpStatus;
  private URI uri;
  private HttpMethod httpMethod;
  private String error;
  @JsonProperty(value = "product")
  T t;

  // HTTP 200 Constructor
  public ProductBatchResult(HttpStatus httpStatus, URI uri, HttpMethod httpMethod, T t) {
    super();
    this.httpStatus = httpStatus;
    this.uri = uri;
    this.httpMethod = httpMethod;
    this.t = t;
  }


  // HTTP 409 Constructor
  public ProductBatchResult(HttpStatus httpStatus, HttpMethod httpMethod, String error, T t) {
    super();
    this.httpStatus = httpStatus;
    this.httpMethod = httpMethod;
    this.error = error;
    this.t = t;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public URI getUri() {
    return uri;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public String getError() {
    return error;
  }


  public T getT() {
    return t;
  }

}
