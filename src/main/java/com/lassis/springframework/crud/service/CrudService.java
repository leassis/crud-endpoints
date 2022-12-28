package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Queue;

public interface CrudService<E extends WithId<ID>, ID extends Serializable> {
    E create(Queue<ID> chain, E obj);

    E update(Queue<ID> chain, ID id, E obj);

    E get(Queue<ID> chain, ID id);

    Page<E> all(Queue<ID> chain, Pageable pageable);

    void deleteById(Queue<ID> chain, ID id);
}
