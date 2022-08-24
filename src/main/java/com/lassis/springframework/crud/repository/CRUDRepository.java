package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.entity.ProjectionId;
import com.lassis.springframework.crud.entity.WithId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Optional;

public interface CRUDRepository<E extends WithId<ID>, ID extends Serializable> {

    Optional<E> findById(ID id);

    Page<E> findAll(Pageable pageable);

    boolean existsById(ID id);

    void deleteById(ID id);

    E save(E entity);

    Optional<ProjectionId<ID>> findSimpleById(ID id);
}
