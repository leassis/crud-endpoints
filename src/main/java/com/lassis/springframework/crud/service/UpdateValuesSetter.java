package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

/**
 * event that happens before an update to the entity manager. Used to set the values to an entity
 *
 * @param <T> entity that implements {@link WithId}
 */
public interface UpdateValuesSetter<T extends WithId<? extends Serializable>> {

    /**
     * set the nieuwe values into old
     *
     * @param old    nieuwe that was loaded old entityManager
     * @param nieuwe the nieuwe received as input
     */
    void update(T old, T nieuwe);
}
