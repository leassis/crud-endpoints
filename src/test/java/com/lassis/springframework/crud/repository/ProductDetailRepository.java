package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.ProductDetail;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailRepository extends PagingAndSortingRepository<ProductDetail, Long> {

    boolean existsByProductIdAndId(Long productId, Long id);
}

