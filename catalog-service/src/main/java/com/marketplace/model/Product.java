package com.marketplace.model;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonRootName;

@Entity
@JsonRootName("product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String productId;
  private String title;
  private String description;
  private String brand;
  private BigDecimal price;
  private String color;

  public Product(Long id, String productId, String title, String description, String brand,
      BigDecimal price, String color) {
    super();
    this.id = id;
    this.productId = productId;
    this.title = title;
    this.description = description;
    this.brand = brand;
    this.price = price;
    this.color = color;
  }

  public Product(String productId, String title, String description, String brand, BigDecimal price,
      String color) {
    super();
    this.productId = productId;
    this.title = title;
    this.description = description;
    this.brand = brand;
    this.price = price;
    this.color = color;
  }

  public Product() {
    super();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((brand == null) ? 0 : brand.hashCode());
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((price == null) ? 0 : price.hashCode());
    result = prime * result + ((productId == null) ? 0 : productId.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Product other = (Product) obj;
    if (brand == null) {
      if (other.brand != null)
        return false;
    } else if (!brand.equals(other.brand))
      return false;
    if (color == null) {
      if (other.color != null)
        return false;
    } else if (!color.equals(other.color))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (price == null) {
      if (other.price != null)
        return false;
    } else if (!price.equals(other.price))
      return false;
    if (productId == null) {
      if (other.productId != null)
        return false;
    } else if (!productId.equals(other.productId))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    return true;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public List<String> nullProperties() {

    List<String> nullFields = new ArrayList<String>();

    for (Field field : this.getClass().getDeclaredFields()) {

      try {
        if (field.get(this) == null)
          nullFields.add(field.getName());
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException("Unable to fetch null fields", e);
      }
    }

    return nullFields;
  }

  @Override
  public String toString() {
    return "Product [id=" + id + ", productId=" + productId + ", title=" + title + ", description="
        + description + ", brand=" + brand + ", price=" + price + ", color=" + color + "]";
  }

}
