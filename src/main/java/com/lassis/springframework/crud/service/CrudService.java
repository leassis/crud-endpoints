package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Queue;

public interface CrudService<E extends WithId<I>, I extends Serializable> {
    E create(Queue<I> chain, E obj);

    E update(Queue<I> chain, I i, E obj);

    E get(Queue<I> chain, I i);

    Page<E> all(Queue<I> chain, Pageable pageable);

    void deleteById(Queue<I> chain, I i);
}
