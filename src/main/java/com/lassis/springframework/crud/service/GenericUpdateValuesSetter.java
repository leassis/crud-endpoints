package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@RequiredArgsConstructor
public class GenericUpdateValuesSetter<T extends WithId<? extends Serializable>> implements UpdateValuesSetter<T> {

    @Override
    public void update(T old, T nieuwe) {
        BeanUtils.copyProperties(nieuwe, old, "id");
    }
}
