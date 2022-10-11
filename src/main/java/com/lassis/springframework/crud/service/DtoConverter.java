package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

public interface DtoConverter<DTO extends Serializable, E extends WithId<? extends Serializable>> {
    E fromDto(DTO obj);

    DTO toDto(E entity);
}
