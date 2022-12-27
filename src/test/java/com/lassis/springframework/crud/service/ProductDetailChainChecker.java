package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
@RequiredArgsConstructor
public class ProductDetailChainChecker implements ChainChecker<ProductDetail, Long> {
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    public boolean exists(Stack<Long> chain, Long id) {
        Long productId = chain.pop();
        return productDetailRepository.existsByProductIdAndId(productId, id);
    }

    @Override
    public boolean exists(Stack<Long> chain) {
        Long productId = chain.pop();
        return productRepository.existsById(productId);
    }

}
