package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Optional;

public interface ParentChildResolver<P extends WithId<ID>, E extends WithId<ID>, ID extends Serializable> {

    void setParent(P parent, E child);

    Optional<P> findParentById(ID parentId);

    Page<E> findAllByParentId(ID parentId, Pageable pageable);

    boolean existsByParentIdAndId(ID parentId, ID childId);

    boolean existsByParentId(ID parentId);
}
