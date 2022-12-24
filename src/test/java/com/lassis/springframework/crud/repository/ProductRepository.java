package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.Product;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {}

