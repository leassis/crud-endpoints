package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;

/**
 * Event that happens before save of an entity. If no implementation is provided no action is done.
 *
 * @param <T> an entity that implements {@link WithId}
 */
public interface BeforeSave<T extends WithId<? extends Serializable>> {

    /**
     * execute the action against the entity. This method could be used to execute common validations
     * against the entity
     *
     * @param obj the entity that implements {@link WithId}
     */
    void execute(T obj);

    static <T extends WithId<? extends Serializable>> BeforeSave<T> none() {
        return obj -> {};
    }
}
