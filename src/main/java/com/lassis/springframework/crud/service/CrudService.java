package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Stack;

public interface CrudService<E extends WithId<ID>, ID extends Serializable> {
    E create(Stack<ID> chain, E obj);

    E update(Stack<ID> chain, ID id, E obj);

    E get(Stack<ID> chain, ID id);

    Page<E> all(Stack<ID> chain, Pageable pageable);

    void deleteById(Stack<ID> chain, ID id);
}
