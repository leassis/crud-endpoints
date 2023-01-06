package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

public interface DtoConverter<I extends Serializable, O extends Serializable, E extends WithId<? extends Serializable>> {
    E fromDto(I obj);

    O toDto(E entity);
}
