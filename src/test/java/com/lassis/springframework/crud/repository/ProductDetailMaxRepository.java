package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.ProductDetailMax;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailMaxRepository extends PagingAndSortingRepository<ProductDetailMax, Long> {

    boolean existsByProductDetailIdAndId(Long productDetailId, Long id);

    Page<ProductDetailMax> findByProductDetailId(Long productDetailId, Pageable pageable);
}
