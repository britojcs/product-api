package com.marketplace.repository.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import com.marketplace.model.Product;

public class ProductWithColorSpec implements Specification<Product> {

  private static final long serialVersionUID = -5350756959973831752L;

  private String color;

  public ProductWithColorSpec(String color) {
    super();
    this.color = color;
  }

  @Override
  public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {

    if (StringUtils.isEmpty(color)) {
      return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
    }

    return criteriaBuilder.like(criteriaBuilder.lower(root.get("color")),
        "%" + this.color.toLowerCase() + "%");
  }

}
