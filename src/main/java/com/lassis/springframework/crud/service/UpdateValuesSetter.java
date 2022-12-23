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
     * convert in to an entity
     *
     * @param entity entity that was loaded from entityManager
     * @param in     the entity received as input
     */
    void update(T entity, T in);
}
