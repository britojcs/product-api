package com.marketplace.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.marketplace.model.Product;

public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {

  Iterable<Product> findByTitleContainingIgnoreCase(String title, Sort sort);

  Iterable<Product> findByDescriptionContainingIgnoreCase(String description, Sort sort);

  Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

}
