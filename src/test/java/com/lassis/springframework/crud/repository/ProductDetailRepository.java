package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDetailRepository extends PagingAndSortingRepository<ProductDetail, Long> {

    boolean existsByProductIdAndId(Long productId, Long id);

    boolean existsByProductId(Long productId);

    Page<ProductDetail> findByProductId(Long productId, Pageable pageable);

    Optional<ProductDetail> findByProductIdAndId(Long productId, Long id);

    void deleteByProductIdAndId(Long productId, Long id);
}
