package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDetailLanguageRepository extends PagingAndSortingRepository<Language, Long> {

    boolean existsByProductDetailIdAndId(Long productDetailId, Long id);

    Page<Language> findByProductDetailId(Long productDetailId, Pageable pageable);
}
