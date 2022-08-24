package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public class EntityAsDtoConverter<T extends WithId<? extends Serializable>>
        implements DtoConverter<T, T> {

    @Override
    public T fromDto(T obj) {
        return obj;
    }

    @Override
    public T toDto(T entity) {
        return entity;
    }

}
