package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.CreateNonEmptyIdException;
import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.exception.UpdateIdConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Stack;

@RequiredArgsConstructor
@Slf4j
public class SimpleCrudService<E extends WithId<ID>, ID extends Serializable> implements CrudService<E, ID> {

    private final PagingAndSortingRepository<E, ID> repository;
    private final BeforeSave<E> beforeSaveAction;
    private final UpdateValuesSetter<E> updateSetter;

    @Override
    public E create(Stack<ID> chain, E obj) {
        if (Objects.nonNull(obj.getId())) {
            throw new CreateNonEmptyIdException();
        }

        logIfMultiLevel(chain);
        return save(chain, obj);
    }

    @Override
    public E update(Stack<ID> chain, ID id, E obj) {
        if (Objects.isNull(obj.getId()) || !Objects.equals(id, obj.getId())) {
            throw new UpdateIdConflictException(id, obj.getId());
        }

        logIfMultiLevel(chain);

        E dbObj = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));

        updateSetter.update(dbObj, obj);
        return save(chain, dbObj);
    }

    @Override
    public E get(Stack<ID> chain, ID id) {
        logIfMultiLevel(chain);

        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public Page<E> all(Stack<ID> chain, Pageable pageable) {
        if (Objects.isNull(pageable)) {
            pageable = Pageable.unpaged();
        }

        logIfMultiLevel(chain);
        return repository.findAll(pageable);
    }

    @Override
    public void deleteById(Stack<ID> chain, ID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(id);
        }
        logIfMultiLevel(chain);
        repository.deleteById(id);
    }

    private E save(Stack<ID> chain, E entity) {
        beforeSaveAction.execute(entity);
        E save = repository.save(entity);
        return get(chain, save.getId());
    }

    private void logIfMultiLevel(Collection<ID> chain) {
        if (!chain.isEmpty()) {
            log.warn("you're using a multi level mapping {} is not doing all verifications", this.getClass());
        }
    }

}
