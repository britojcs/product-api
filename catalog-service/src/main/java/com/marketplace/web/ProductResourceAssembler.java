package com.marketplace.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;
import com.marketplace.model.Product;

/**
 * 
 * Creates a HATEOAS {@link Resource} for a {@link Product}
 * 
 * Provides link for all actions available on a product resource
 * 
 * <pre>
 * {@code
 * {
 * "ProductResource": {
 *   "productId": "GAS1234567",
 *   "title": "Jeans",
 *   "description": "Slim fit jeans",
 *   "brand": "GAS",
 *   "price": 10000.0,
 *   "color": "Blue",
 *   "_links": {
 *     "self": {
 *       "href": "http://localhost:8762/api/products/10001",
 *       "type": "GET"
 *     },
 *     "create": {
 *       "href": "http://localhost:8762/api/products",
 *       "type": "POST"
 *     },
 *     "createBatch": {
 *       "href": "http://localhost:8762/api/products/batch",
 *       "type": "POST"
 *     },
 *     "updateOrCreate": {
 *       "href": "http://localhost:8762/api/products/10001",
 *       "type": "PUT"
 *     },
 *     "update": {
 *       "href": "http://localhost:8762/api/products/10001",
 *       "type": "PATCH"
 *     },
 *     "delete": {
 *       "href": "http://localhost:8762/api/products/10001",
 *       "type": "DELETE"
 *     }
 *   }
 *  }
 * }
 * </pre>
 * 
 * @author Nikhil Vasaikar
 *
 */
@Component
public class ProductResourceAssembler extends ResourceAssemblerSupport<Product, ProductResource> {

  public ProductResourceAssembler() {
    super(ProductHalController.class, ProductResource.class);
  }

  @Override
  public ProductResource toResource(Product product) {

    ProductResource productResource = new ProductResource(product);
    productResource.add(
        linkTo(ProductHalController.class).slash(product.getId()).withSelfRel().withType("GET"));

    productResource.add(linkTo(methodOn(ProductHalController.class).newProduct((product)))
        .withRel("create").withType("POST"));
    productResource
        .add(linkTo(methodOn(ProductHalController.class).newProducts(new ProductBatch<Product>()))
            .withRel("createBatch").withType("POST"));
    productResource
        .add(linkTo(methodOn(ProductHalController.class).updateProductPut(product, product.getId()))
            .withRel("updateOrCreate").withType("PUT"));
    productResource.add(
        linkTo(methodOn(ProductHalController.class).updateProductPatch(product, product.getId()))
            .withRel("update").withType("PATCH"));
    productResource.add(
        linkTo(methodOn(ProductHalController.class).updateProductPatch(product, product.getId()))
            .withRel("delete").withType("DELETE"));

    return productResource;
  }

}
