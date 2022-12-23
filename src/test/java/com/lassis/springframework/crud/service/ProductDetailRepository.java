package com.lassis.springframework.crud.service;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepository extends PagingAndSortingRepository<ProductDetail, Long> {}

