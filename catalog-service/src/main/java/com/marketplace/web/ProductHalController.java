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
import org.springframework.data.jpa.domain.Specification;
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
import com.marketplace.repository.specs.ProductWithBrandSpec;
import com.marketplace.repository.specs.ProductWithColorSpec;
import com.marketplace.repository.specs.ProductWithDescriptionSpec;
import com.marketplace.repository.specs.ProductWithProductIdSpec;
import com.marketplace.repository.specs.ProductWithTitleSpec;

/**
 * Product API endpoints for HATEOAS response format
 * 
 * @author Nikhil Vasaikar
 *
 */
@RestController
@RequestMapping("/products")
public class ProductHalController extends AbstractProductController {


  private ProductResourceAssembler productResourceAssembler;

  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @Autowired
  public ProductHalController(ProductRepository productRepository,
      ProductResourceAssembler productResourceAssembler,
      PagedResourcesAssembler<Product> pagedResourcesAssembler) {
    super(productRepository);
    this.productResourceAssembler = productResourceAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @GetMapping(produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<PagedResources<?>> findAll(String productId, String title, String color,
      String brand, String description, Pageable pageable) {


    Page<Product> page = productRepository.findAll(
        Specification.where(new ProductWithProductIdSpec(productId))
            .and(new ProductWithTitleSpec(title)).and(new ProductWithColorSpec(color))
            .and(new ProductWithBrandSpec(brand)).and(new ProductWithDescriptionSpec(description)),
        pageable);

    return page.getTotalElements() != 0
        ? ResponseEntity.ok(pagedResourcesAssembler.toResource(page, productResourceAssembler))
        : ResponseEntity.ok(pagedResourcesAssembler.toEmptyResource(page, Product.class));
  }

  @GetMapping(value = "/{id}", produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<ProductResource> findById(@PathVariable Long id) {

    return productRepository.findById(id)
        .map(product -> productResourceAssembler.toResource(product)).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(produces = MarketPlaceMediaTypes.V1_HAL_UTF8)
  public ResponseEntity<?> newProduct(@RequestBody Product product) {

    try {

      ResponseEntity<?> responseEntity = null;

      if (productExists(product)) {

        responseEntity = ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Product with Id : " + product.getProductId()
                + " already exists. Please send a PUT/PATCH request to update this product");
      } else {

        Product savedProduct = productRepository.save(product);
        ProductResource productResource = productResourceAssembler.toResource(savedProduct);

        responseEntity = ResponseEntity.created(new URI(productResource.getId().getHref()))
            .body(productResource);
      }
      return responseEntity;

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

        if (productExists(product)) {

          productResourceBatchResults
              .add(new ProductBatchResult<Product>(HttpStatus.CONFLICT, HttpMethod.POST,
                  "Product with Id : " + product.getProductId()
                      + " already exists. Please send a PUT/PATCH request to update this product",
                  product));
        } else {

          Product savedProduct = productRepository.save(product);
          ProductResource savedProductResource = productResourceAssembler.toResource(savedProduct);

          productResourceBatchResults.add(new ProductBatchResult<ProductResource>(HttpStatus.OK,
              new URI(savedProductResource.getId().getHref()), HttpMethod.POST,
              savedProductResource));
        }
      } catch (Exception e) {

        productResourceBatchResults.add(new ProductBatchResult<Product>(HttpStatus.BAD_REQUEST,
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
