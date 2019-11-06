package com.marketplace.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.specs.ProductWithBrandSpec;
import com.marketplace.repository.specs.ProductWithColorSpec;
import com.marketplace.repository.specs.ProductWithDescriptionSpec;
import com.marketplace.repository.specs.ProductWithProductIdSpec;
import com.marketplace.repository.specs.ProductWithTitleSpec;

/**
 * Product API endpoints for plain JSON response format
 * 
 * @author Nikhil Vasaikar
 *
 */
@RestController
@RequestMapping("/products")
public class ProductController extends AbstractProductController {

  @Autowired
  public ProductController(ProductRepository productRepository) {
    super(productRepository);
  }

  @GetMapping(produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  public ResponseEntity<Iterable<Product>> findAll(String productId, String title, String color,
      String brand, String description, Sort sort) {

    return ResponseEntity.ok(productRepository.findAll(
        Specification.where(new ProductWithProductIdSpec(productId))
            .and(new ProductWithTitleSpec(title)).and(new ProductWithColorSpec(color))
            .and(new ProductWithBrandSpec(brand)).and(new ProductWithDescriptionSpec(description)),
        sort));
  }

  @GetMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  public ResponseEntity<Product> findById(@PathVariable Long id) {

    return productRepository.findById(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  ResponseEntity<?> newProduct(@RequestBody Product product) {

    try {

      ResponseEntity<?> responseEntity = null;

      if (productExists(product)) {

        responseEntity = ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Product with Id : " + product.getProductId()
                + " already exists. Please send a PUT/PATCH request to update this product");
      } else {

        Product savedProduct = productRepository.save(product);
        responseEntity =
            ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .path("/" + savedProduct.getId().toString()).build().toUri())
                .body(savedProduct);
      }

      return responseEntity;

    } catch (Exception e) {

      return ResponseEntity.badRequest().body("Unable to create " + product);
    }
  }

  @PostMapping(value = "/batch", produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  ResponseEntity<ProductBatch<ProductBatchResult<Product>>> newProducts(
      @RequestBody ProductBatch<Product> productBatch) {

    List<ProductBatchResult<Product>> productBatchResults =
        new ArrayList<ProductBatchResult<Product>>();

    for (Product product : productBatch.getProducts()) {

      try {

        if (productExists(product)) {

          productBatchResults
              .add(new ProductBatchResult<Product>(HttpStatus.CONFLICT, HttpMethod.POST,
                  "Product with Id : " + product.getProductId()
                      + " already exists. Please send a PUT/PATCH request to update this product",
                  product));
        } else {

          Product savedProduct = productRepository.save(product);

          productBatchResults.add(new ProductBatchResult<Product>(HttpStatus.OK,
              ServletUriComponentsBuilder.fromCurrentRequestUri()
                  .path("/" + savedProduct.getId().toString()).build().toUri(),
              HttpMethod.POST, savedProduct));
        }
      } catch (Exception e) {

        productBatchResults.add(new ProductBatchResult<Product>(HttpStatus.BAD_REQUEST,
            HttpMethod.POST, e.getMessage(), product));
      }
    }

    return ResponseEntity.status(HttpStatus.MULTI_STATUS)
        .body(new ProductBatch<ProductBatchResult<Product>>(productBatchResults));
  }

  @PutMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  ResponseEntity<?> updateProductPut(@RequestBody Product product, @PathVariable Long id) {

    Optional<Product> existingProduct = productRepository.findById(id);

    try {

      product.setId(id);
      Product savedProduct = productRepository.save(product);

      return existingProduct.isPresent() ? ResponseEntity.ok(savedProduct)
          : ResponseEntity
              .created(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri())
              .body(savedProduct);

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Unable to create " + product);
    }
  }

  @PatchMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  ResponseEntity<?> updateProductPatch(@RequestBody Product product, @PathVariable Long id) {

    ResponseEntity<?> responseEntity = null;
    Product existingProduct = productRepository.findById(id).orElse(null);

    if (existingProduct == null) {
      responseEntity = ResponseEntity.notFound().build();

    } else {
      try {
        BeanUtils.copyProperties(product, existingProduct,
            product.nullProperties().toArray(new String[product.nullProperties().size()]));
        productRepository.save(existingProduct);
        responseEntity = ResponseEntity.ok(existingProduct);

      } catch (Exception e) {
        responseEntity = ResponseEntity.badRequest().body("Unable to delete product with id " + id);
      }
    }

    return responseEntity;
  }

  @DeleteMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_JSON_UTF8)
  ResponseEntity<?> deleteProduct(@PathVariable Long id) {

    ResponseEntity<?> responseEntity = null;
    Optional<Product> existingProduct = productRepository.findById(id);
    if (!existingProduct.isPresent()) {

      responseEntity = ResponseEntity.notFound().build();
    } else {
      try {
        productRepository.deleteById(id);
        responseEntity = ResponseEntity.ok().build();

      } catch (Exception e) {
        responseEntity = ResponseEntity.badRequest().body("Unable to delete product with id " + id);
      }
    }

    return responseEntity;
  }

  public void setProductRepository(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

}
