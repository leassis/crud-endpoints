package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

public interface SingleDtoConverter<I extends Serializable, E extends WithId<? extends Serializable>>
        extends DtoConverter<I, I, E> {}
