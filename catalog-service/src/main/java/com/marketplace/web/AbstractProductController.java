package com.marketplace.web;

import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;

public abstract class AbstractProductController {
  
  protected ProductRepository productRepository;
  
  public AbstractProductController(ProductRepository productRepository) {
    super();
    this.productRepository = productRepository;
  }



  protected boolean productExists(Product product) {
    
    return productRepository.existsByProductId(product.getProductId());
  }

}
