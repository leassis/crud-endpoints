package com.lassis.springframework.crud.service;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class ProductDetailUpdateValuesSetter implements UpdateValuesSetter<ProductDetail> {

    @Override
    public void update(ProductDetail entity, ProductDetail in) {
        if (isNull(entity.getId()) && nonNull(in.getId())) {
            entity.setId(in.getId());
        }

        entity.setDetail(in.getDetail());
    }
}

