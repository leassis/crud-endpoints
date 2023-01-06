package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Optional;

public interface ParentChildResolver<P extends WithId<I>, E extends WithId<I>, I extends Serializable> {

    void setParent(P parent, E child);

    Optional<P> findParentById(I parentI);

    Page<E> findAllByParentId(I parentI, Pageable pageable);

    boolean existsByParentIdAndId(I parentI, I childI);

    boolean existsByParentId(I parentI);
}
