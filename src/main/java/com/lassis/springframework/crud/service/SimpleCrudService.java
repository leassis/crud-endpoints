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
import java.util.Queue;

@RequiredArgsConstructor
@Slf4j
public class SimpleCrudService<E extends WithId<I>, I extends Serializable> implements CrudService<E, I> {
    private final PagingAndSortingRepository<E, I> repository;
    private final BeforeSave<E> beforeSaveAction;
    private final UpdateValuesSetter<E> updateSetter;

    @Override
    public E create(Queue<I> chain, E obj) {
        failIfMultiLevel(chain);

        if (Objects.nonNull(obj.getId())) {
            throw new CreateNonEmptyIdException();
        }

        return save(obj);
    }

    @Override
    public E update(Queue<I> chain, I i, E obj) {
        failIfMultiLevel(chain);

        if (Objects.nonNull(obj.getId()) && !Objects.equals(i, obj.getId())) {
            throw new UpdateIdConflictException(i, obj.getId());
        }

        E dbObj = repository.findById(i)
                .orElseThrow(() -> new NotFoundException(i));

        updateSetter.update(dbObj, obj);
        return save(dbObj);
    }

    @Override
    public E get(Queue<I> chain, I i) {
        failIfMultiLevel(chain);

        return repository.findById(i)
                .orElseThrow(() -> new NotFoundException(i));
    }

    @Override
    public Page<E> all(Queue<I> chain, Pageable pageable) {
        failIfMultiLevel(chain);

        return repository.findAll(pageable);
    }

    @Override
    public void deleteById(Queue<I> chain, I i) {
        failIfMultiLevel(chain);

        if (!repository.existsById(i)) {
            throw new NotFoundException(i);
        }

        repository.deleteById(i);
    }

    private E save(E entity) {
        beforeSaveAction.execute(entity);
        E save = repository.save(entity);

        return repository.findById(save.getId())
                .orElseThrow(() -> new NotFoundException(save.getId()));
    }


    private void failIfMultiLevel(Collection<I> chain) {
        if (!chain.isEmpty()) {
            log.error("{} can only be used with single level endpoints, define a primary CrudService to this entity", this.getClass());
            throw new IllegalStateException(this.getClass() + " can only be used with single level endpoints, define a primary CrudService to this entity");
        }
    }
}
