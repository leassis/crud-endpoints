package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
import com.lassis.springframework.crud.service.TwoLevelCrudService;
import com.lassis.springframework.crud.service.TwoLevelRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Configuration
public class CrudServiceConfig {

    @Bean
    @Primary
    CrudService<ProductDetail, Long> productDetailCrudService(ProductRepository productRepository,
                                                              ProductDetailRepository productDetailRepository,
                                                              CrudService<ProductDetail, Long> service) {

        return new TwoLevelCrudService<>(service, new TwoLevelRepository<Product, ProductDetail, Long>() {
            @Override
            public void setParent(Product parent, ProductDetail child) {
                child.setProduct(parent);
            }

            @Override
            public Optional<Product> findParentById(Long parentId) {
                return productRepository.findById(parentId);
            }

            @Override
            public Optional<ProductDetail> findByParentIdAndId(Long parentId, Long childId) {
                return productDetailRepository.findByProductIdAndId(parentId, childId);
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

            @Override
            public void deleteByParentIdAndId(Long parentId, Long childId) {
                productDetailRepository.deleteByProductIdAndId(parentId, childId);
            }
        });

    }
}
