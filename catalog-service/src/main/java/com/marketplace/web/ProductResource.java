package com.marketplace.web;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import com.marketplace.model.Product;

/**
 * A HATEOAS {@link Resource} for {@link Product}
 *
 * @author Nikhil Vasaikar
 *
 */
public class ProductResource extends Resource<Product> {

  public ProductResource(Product product, Link... links) {
    super(product, links);
  }

}
