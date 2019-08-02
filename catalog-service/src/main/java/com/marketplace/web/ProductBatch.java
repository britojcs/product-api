package com.marketplace.web;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.marketplace.model.Product;

/**
 * 
 * A {@link Product} collection wrapper used for batch insertion of products. Mainly used to wrap
 * the batch insert request into an object
 * 
 * <pre>
 * {@code
 * {
 *   "productsBatch": {
 *        "products":[ 
 *             {
 *                 "productId": "ARMBLT101",
 *                 "title": "Belt",
 *                 "description": "Reversible genuine leather belt",
 *                 "brand": "Armani",
 *                 "price": 3500,
 *                 "color": "Black/Brown"
 *             },
 *             {
 *                 "productId": "COALTHBG102",
 *                 "title": "Cardigan",
 *                 "description": "Cashmere cardigan with henley collar",
 *                 "brand": "Ralph Lauren",
 *                 "price": 9000,
 *                 "color": "Charcoal Black"
 *             }
 *         ]
 *    }
 * }
 * }
 * </pre>
 * 
 * 
 * </code>
 * 
 * @author Nikhil Vasaikar
 *
 * @param <T> {@link Product} or {@link ProductResource}
 * 
 * @see {@link ProductController#newProducts(ProductBatch)},
 *      {@link ProductHalController#newProducts(ProductBatch)}
 */
@JsonRootName("productsBatch")
public class ProductBatch<T> {

  private List<T> products;

  public ProductBatch() {
    this.products = new ArrayList<T>();
  }


  public ProductBatch(List<T> products) {
    super();
    this.products = products;
  }



  public void addToProducts(T product) {
    this.products.add(product);
  }

  public List<T> getProducts() {
    return products;
  }

  public void setProducts(List<T> products) {
    this.products = products;
  }

}
