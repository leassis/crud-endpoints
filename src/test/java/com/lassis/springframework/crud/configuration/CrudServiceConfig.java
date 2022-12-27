package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
import com.lassis.springframework.crud.service.TwoLevelCrudService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CrudServiceConfig {

    @Bean
    @Primary
    CrudService<ProductDetail, Long> productDetailCrudService(ProductRepository productRepository,
                                                              ProductDetailRepository productDetailRepository,
                                                              CrudService<ProductDetail, Long> service) {

        return TwoLevelCrudService.<ProductDetail, Product, Long>builder()
                .delegateTo(service)
                .parentSetter((product, productDetail) -> productDetail.setProduct(product))
                .existsByParentId(productRepository::existsById)
                .existsByParentIdAndId(productDetailRepository::existsByProductIdAndId)
                .findAllByParentId(productDetailRepository::findByProductId)
                .findByParentIdAndId(productDetailRepository::findByProductIdAndId)
                .findParentById(productRepository::findById)
                .deleteByParentIdAndId(productDetailRepository::deleteByProductIdAndId)
                .build();
    }
}
