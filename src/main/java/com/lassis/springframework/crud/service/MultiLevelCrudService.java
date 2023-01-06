package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.exception.RelationshipNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Queue;

@RequiredArgsConstructor
public class MultiLevelCrudService<P extends WithId<I>, E extends WithId<I>, I extends Serializable> implements CrudService<E, I> {
    private final CrudService<E, I> delegateTo;
    private final ParentChildResolver<P, E, I> parentChildResolver;

    @Override
    public E create(Queue<I> chain, E obj) {
        I parentI = chain.remove();

        if (chain.isEmpty()) {
            P entity = parentChildResolver.findParentById(parentI)
                    .orElseThrow(() -> new NotFoundException(parentI));
            parentChildResolver.setParent(entity, obj);
        } else {
            if (!parentChildResolver.existsByParentIdAndId(parentI, chain.peek())){
                throw new RelationshipNotFoundException();
            }
        }

        return delegateTo.create(chain, obj);
    }

    @Override
    public E update(Queue<I> chain, I i, E obj) {
        I parentI = chain.remove();

        I childI = chain.isEmpty() ? obj.getId() : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentI, childI)) {
            throw new RelationshipNotFoundException();
        }

        return delegateTo.update(chain, i, obj);
    }

    @Override
    public E get(Queue<I> chain, I i) {
        I parentI = chain.remove();
        I childI = chain.isEmpty() ? i : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentI, childI)) {
            throw new RelationshipNotFoundException();
        }
        return delegateTo.get(chain, i);
    }

    @Override
    public Page<E> all(Queue<I> chain, Pageable pageable) {
        I parentI = chain.remove();

        if (chain.isEmpty()) {
            if (!parentChildResolver.existsByParentId(parentI)) {
                throw new NotFoundException();
            }
            return parentChildResolver.findAllByParentId(parentI, pageable);
        } else {
            if (!parentChildResolver.existsByParentIdAndId(parentI, chain.peek())) {
                throw new RelationshipNotFoundException();
            }
            return delegateTo.all(chain, pageable);
        }
    }

    @Override
    public void deleteById(Queue<I> chain, I i) {
        I parentI = chain.remove();
        I childI = chain.isEmpty() ? i : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentI, childI)) {
            throw new RelationshipNotFoundException();
        }
        delegateTo.deleteById(chain, i);
    }
}
