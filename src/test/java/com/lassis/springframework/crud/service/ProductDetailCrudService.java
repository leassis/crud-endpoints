package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Stack;

@Component
@Primary
@RequiredArgsConstructor
public class ProductDetailCrudService implements CrudService<ProductDetail, Long> {
    private final CrudService<ProductDetail, Long> crudService;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    public ProductDetail create(Stack<Long> chain, ProductDetail obj) {
        Long productId = chain.pop();
        failIfNotExist(productId);

        return crudService.create(chain, obj);
    }

    @Override
    public ProductDetail update(Stack<Long> chain, Long id, ProductDetail obj) {
        Long productId = chain.pop();
        failIfNotExist(productId);

        return crudService.update(chain, id, obj);
    }

    @Override
    public ProductDetail get(Stack<Long> chain, Long id) {
        return productDetailRepository.findByProductIdAndId(chain.pop(), id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public Page<ProductDetail> all(Stack<Long> chain, Pageable pageable) {
        if (Objects.isNull(pageable)) {
            pageable = Pageable.unpaged();
        }

        Long productId = chain.pop();
        failIfNotExist(productId);

        return productDetailRepository.findByProductId(productId, pageable);
    }

    @Override
    public void deleteById(Stack<Long> chain, Long id) {
        Long productId = chain.pop();
        failIfNotExist(productId);

        crudService.deleteById(chain, id);
    }

    private void failIfNotExist(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException(productId);
        }
    }

}
