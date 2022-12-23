package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.CreateNonEmptyIdException;
import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.exception.UpdateIdConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.Objects;

@RequiredArgsConstructor
public class CrudService<E extends WithId<ID>, ID extends Serializable> {

    private final PagingAndSortingRepository<E, ID> repository;
    private final BeforeSave<E> beforeSaveAction;
    private final UpdateValuesSetter<E> updateSetter;

    public E create(E obj) {
        if (Objects.nonNull(obj.getId())) {
            throw new CreateNonEmptyIdException();
        }

        return save(obj);
    }

    public E update(ID id, E obj) {
        if (Objects.isNull(obj.getId()) || !Objects.equals(id, obj.getId())) {
            throw new UpdateIdConflictException(id, obj.getId());
        }

        E dbObj = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));

        updateSetter.update(dbObj, obj);
        return save(dbObj);
    }

    public E get(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    public boolean exists(ID id) {
        return repository.existsById(id);
    }

    public Page<E> findAll(Pageable pageable) {
        if (Objects.isNull(pageable)) {
            pageable = Pageable.unpaged();
        }
        return repository.findAll(pageable);
    }

    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(id);
        }
        repository.deleteById(id);
    }

    private E save(E entity) {
        beforeSaveAction.execute(entity);
        E save = repository.save(entity);
        return get(save.getId());
    }


}
