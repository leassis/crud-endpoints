package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.repository.ProductDetailLanguageRepository;
import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.Language;
import com.lassis.springframework.crud.service.ParentChildResolver;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Configuration
public class CrudServiceConfig {

    @Bean
    ParentChildResolver<Product, ProductDetail, Long> productDetailTwoLevelRepository(ProductRepository productRepository,
                                                                                      ProductDetailRepository productDetailRepository) {

        return new ParentChildResolver<Product, ProductDetail, Long>() {
            @Override
            public void setParent(Product parent, ProductDetail child) {
                child.setProduct(parent);
            }

            @Override
            public Optional<Product> findParentById(Long parentId) {
                return productRepository.findById(parentId);
            }

            @Override
            public Page<ProductDetail> findAllByParentId(Long parentId, Pageable pageable) {
                return productDetailRepository.findByProductId(parentId, pageable);
            }

            @Override
            public boolean existsByParentIdAndId(Long parentId, Long childId) {
                return productDetailRepository.existsByProductIdAndId(parentId, childId);
            }

            @Override
            public boolean existsByParentId(Long parentId) {
                return productRepository.existsById(parentId);
            }

        };

    }

    @Bean
    ParentChildResolver<ProductDetail, Language, Long> productDetailMaxTwoLevelRepository(ProductDetailRepository parentRepository,
                                                                                          ProductDetailLanguageRepository childRepository) {

        return new ParentChildResolver<ProductDetail, Language, Long>() {
            @Override
            public void setParent(ProductDetail parent, Language child) {
                child.setProductDetail(parent);
            }

            @Override
            public Optional<ProductDetail> findParentById(Long parentId) {
                return parentRepository.findById(parentId);
            }

            @Override
            public Page<Language> findAllByParentId(Long parentId, Pageable pageable) {
                return childRepository.findByProductDetailId(parentId, pageable);
            }

            @Override
            public boolean existsByParentIdAndId(Long parentId, Long childId) {
                return childRepository.existsByProductDetailIdAndId(parentId, childId);
            }

            @Override
            public boolean existsByParentId(Long parentId) {
                return parentRepository.existsById(parentId);
            }

        };

    }

}
