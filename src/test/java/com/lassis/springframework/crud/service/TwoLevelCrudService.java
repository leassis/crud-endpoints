package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.NotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Builder
public class TwoLevelCrudService<E extends WithId<ID>, P extends WithId<ID>, ID extends Serializable> implements CrudService<E, ID> {
    private final CrudService<E, ID> delegateTo;

    private final BiConsumer<P, E> parentSetter;

    private final Function<ID, Optional<P>> findParentById;
    private final BiFunction<ID, ID, Optional<E>> findByParentIdAndId;
    private final BiFunction<ID, Pageable, Page<E>> findAllByParentId;

    private final BiPredicate<ID, ID> existsByParentIdAndId;
    private final Predicate<ID> existsByParentId;

    private final BiConsumer<ID, ID> deleteByParentIdAndId;

    @Override
    public E create(Stack<ID> chain, E obj) {
        ID productId = chain.pop();

        P entity = findParentById.apply(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        parentSetter.accept(entity, obj);

        return delegateTo.create(chain, obj);
    }

    @Override
    public E update(Stack<ID> chain, ID id, E obj) {
        ID parentId = chain.pop();

        if (!existsByParentIdAndId.test(parentId, obj.getId())) {
            throw new NotFoundException(id);
        }

        return delegateTo.update(chain, id, obj);
    }

    @Override
    public E get(Stack<ID> chain, ID id) {
        ID productId = chain.pop();
        return findByParentIdAndId.apply(productId, id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public Page<E> all(Stack<ID> chain, Pageable pageable) {
        ID parentId = chain.pop();
        if (!existsByParentId.test(parentId)) {
            throw new NotFoundException(parentId);
        }

        return findAllByParentId.apply(parentId, pageable);
    }

    @Override
    public void deleteById(Stack<ID> chain, ID id) {
        ID parentId = chain.pop();
        deleteByParentIdAndId.accept(parentId, id);
    }
}
