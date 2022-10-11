package com.lassis.springframework.crud.service;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class ProductUpdateValuesSetter implements UpdateValuesSetter<Product> {

    @Override
    public void update(Product entity, Product in) {
        if (isNull(entity.getId()) && nonNull(in.getId())) {
            entity.setId(in.getId());
        }

        entity.setName(in.getName());
    }
}

