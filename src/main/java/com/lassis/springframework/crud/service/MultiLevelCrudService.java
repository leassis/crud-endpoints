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
public class MultiLevelCrudService<P extends WithId<ID>, E extends WithId<ID>, ID extends Serializable> implements CrudService<E, ID> {
    private final CrudService<E, ID> delegateTo;
    private final ParentChildResolver<P, E, ID> parentChildResolver;

    @Override
    public E create(Queue<ID> chain, E obj) {
        ID parentId = chain.remove();

        if (chain.isEmpty()) {
            P entity = parentChildResolver.findParentById(parentId)
                    .orElseThrow(() -> new NotFoundException(parentId));
            parentChildResolver.setParent(entity, obj);
        } else {
            if (!parentChildResolver.existsByParentIdAndId(parentId, chain.peek())){
                throw new RelationshipNotFoundException();
            }
        }

        return delegateTo.create(chain, obj);
    }

    @Override
    public E update(Queue<ID> chain, ID id, E obj) {
        ID parentId = chain.remove();

        ID childId = chain.isEmpty() ? obj.getId() : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentId, childId)) {
            throw new RelationshipNotFoundException();
        }

        return delegateTo.update(chain, id, obj);
    }

    @Override
    public E get(Queue<ID> chain, ID id) {
        ID parentId = chain.remove();
        ID childId = chain.isEmpty() ? id : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentId, childId)) {
            throw new RelationshipNotFoundException();
        }
        return delegateTo.get(chain, id);
    }

    @Override
    public Page<E> all(Queue<ID> chain, Pageable pageable) {
        ID parentId = chain.remove();

        if (chain.isEmpty()) {
            if (!parentChildResolver.existsByParentId(parentId)) {
                throw new NotFoundException();
            }
            return parentChildResolver.findAllByParentId(parentId, pageable);
        } else {
            if (!parentChildResolver.existsByParentIdAndId(parentId, chain.peek())) {
                throw new RelationshipNotFoundException();
            }
            return delegateTo.all(chain, pageable);
        }
    }

    @Override
    public void deleteById(Queue<ID> chain, ID id) {
        ID parentId = chain.remove();
        ID childId = chain.isEmpty() ? id : chain.peek();
        if (!parentChildResolver.existsByParentIdAndId(parentId, childId)) {
            throw new RelationshipNotFoundException();
        }
        delegateTo.deleteById(chain, id);
    }
}
