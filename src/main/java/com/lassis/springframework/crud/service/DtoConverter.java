package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

public interface DtoConverter<D extends Serializable, E extends WithId<? extends Serializable>> {
    E fromDto(D obj);

    D toDto(E entity);
}
