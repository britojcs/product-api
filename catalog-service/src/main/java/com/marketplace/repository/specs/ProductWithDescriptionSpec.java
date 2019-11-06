package com.marketplace.repository.specs;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import com.marketplace.model.Product;

public class ProductWithDescriptionSpec implements Specification<Product> {

  private static final long serialVersionUID = -5350756959973831752L;

  private String description;

  public ProductWithDescriptionSpec(String description) {
    super();
    this.description = description;
  }

  @Override
  public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {

    if (StringUtils.isEmpty(description)) {
      return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
    }

    return criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
        "%" + this.description.toLowerCase() + "%");
  }

}
