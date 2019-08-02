package com.marketplace.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
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
import com.marketplace.model.Product;
import com.marketplace.repository.ProductRepository;

/**
 * Product API endpoints for HATEOAS response format
 * 
 * @author Nikhil Vasaikar
 *
 */
@RestController
@RequestMapping("/products")
public class ProductHalController {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductResourceAssembler productResourceAssembler;

  @Autowired
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @GetMapping(produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<PagedResources<ProductResource>> findAll(Pageable pageable) {

    return ResponseEntity.ok(pagedResourcesAssembler.toResource(productRepository.findAll(pageable),
        productResourceAssembler));
  }

  @GetMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<ProductResource> findById(@PathVariable Long id) {

    return productRepository.findById(id)
        .map(product -> productResourceAssembler.toResource(product)).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(value = "/search/findByDescription", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<PagedResources<?>> findByDescription(String description,
      Pageable pageable) {

    Page<Product> page =
        productRepository.findByDescriptionContainingIgnoreCase(description, pageable);
    return page.getTotalElements() != 0
        ? ResponseEntity.ok(pagedResourcesAssembler.toResource(page, productResourceAssembler))
        : ResponseEntity.ok(pagedResourcesAssembler.toEmptyResource(page, Product.class));

  }

  @GetMapping(value = "/search/findByTitle", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<PagedResources<?>> findByTitle(String title, Pageable pageable) {

    Page<Product> page = productRepository.findByTitleContainingIgnoreCase(title, pageable);
    return page.getTotalElements() != 0
        ? ResponseEntity.ok(pagedResourcesAssembler.toResource(page, productResourceAssembler))
        : ResponseEntity.ok(pagedResourcesAssembler.toEmptyResource(page, Product.class));

  }

  @PostMapping(produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<?> newProduct(@RequestBody Product product) {

    try {

      Product savedProduct = productRepository.save(product);
      ProductResource productResource = productResourceAssembler.toResource(savedProduct);

      return ResponseEntity.created(new URI(productResource.getId().getHref()))
          .body(productResource);

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Unable to create " + product);
    }
  }

  @PostMapping(value = "/batch", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  ResponseEntity<Resource<ProductBatch<ProductBatchResult<?>>>> newProducts(
      @RequestBody ProductBatch<Product> productBatch) {

    List<ProductBatchResult<?>> productResourceBatchResults =
        new ArrayList<ProductBatchResult<?>>();

    for (Product product : productBatch.getProducts()) {

      try {

        Product savedProduct = productRepository.save(product);
        ProductResource savedProductResource = productResourceAssembler.toResource(savedProduct);

        productResourceBatchResults.add(new ProductBatchResult<ProductResource>(HttpStatus.OK,
            new URI(savedProductResource.getId().getHref()), HttpMethod.POST,
            savedProductResource));


      } catch (Exception e) {

        productResourceBatchResults.add(new ProductBatchResult<Product>(HttpStatus.CONFLICT,
            HttpMethod.POST, e.getMessage(), product));
      }
    }

    return ResponseEntity.status(HttpStatus.MULTI_STATUS)
        .body(new Resource<ProductBatch<ProductBatchResult<?>>>(
            new ProductBatch<ProductBatchResult<?>>(productResourceBatchResults),
            linkTo(methodOn(ProductHalController.class).newProducts(productBatch)).withSelfRel()
                .withType("POST")));
  }

  @PutMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  ResponseEntity<?> updateProductPut(@RequestBody Product product, @PathVariable Long id) {

    Optional<Product> existingProduct = productRepository.findById(id);

    try {

      product.setId(id);
      Product savedProduct = productRepository.save(product);
      ProductResource productResource = productResourceAssembler.toResource(savedProduct);

      return existingProduct.isPresent() ? ResponseEntity.ok(productResource)
          : ResponseEntity.created(new URI(productResource.getId().getHref()))
              .body(productResource);

    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Unable to create " + product);
    }
  }

  @PatchMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
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
        ProductResource productResource = productResourceAssembler.toResource(existingProduct);
        responseEntity = ResponseEntity.ok(productResource);

      } catch (Exception e) {
        responseEntity = ResponseEntity.badRequest().body("Unable to delete product with id " + id);
      }
    }

    return responseEntity;

  }

  @DeleteMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
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
