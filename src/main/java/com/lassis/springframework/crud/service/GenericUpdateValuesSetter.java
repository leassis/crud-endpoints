package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class GenericUpdateValuesSetter<T extends WithId<? extends Serializable>> implements UpdateValuesSetter<T> {
    private final Class<T> clazz;

    @Override
    public void update(T from, T to) {
        if (isNull(to.getId()) && nonNull(from.getId())) {
            Method setId = BeanUtils.findMethod(clazz, "setId");
            try {
                assert setId != null;
                setId.invoke(to, from.getId());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        BeanUtils.copyProperties(from, to);
    }
}
