package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

/**
 * event that happens before an update to the entity manager. Used to set the values to an entity
 *
 * @param <T> entity that implements {@link WithId}
 * @param <T> a dto
 */
public interface UpdateValuesSetter<T extends WithId<? extends Serializable>> {

    /**
     * convert from to an to
     *
     * @param from the to received as input
     * @param to   to that was loaded from entityManager
     */
    void update(T from, T to);
}
