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
    private final boolean ioExecuteFindAll;

    @Override
    public E create(Queue<ID> chain, E obj) {
        ID productId = chain.remove();

        P entity = parentChildResolver.findParentById(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        parentChildResolver.setParent(entity, obj);

        return delegateTo.create(chain, obj);
    }

    @Override
    public E update(Queue<ID> chain, ID id, E obj) {
        ID parentId = chain.remove();

        if (!parentChildResolver.existsByParentIdAndId(parentId, obj.getId())) {
            throw new RelationshipNotFoundException();
        }

        return delegateTo.update(chain, id, obj);
    }

    @Override
    public E get(Queue<ID> chain, ID id) {
        ID parentId = chain.remove();
        if (!parentChildResolver.existsByParentIdAndId(parentId, id)) {
            throw new RelationshipNotFoundException();
        }
        return delegateTo.get(chain, id);
    }

    @Override
    public Page<E> all(Queue<ID> chain, Pageable pageable) {
        ID parentId = chain.remove();
        if (!parentChildResolver.existsByParentId(parentId)) {
            throw new NotFoundException(parentId);
        }

        return ioExecuteFindAll ? parentChildResolver.findAllByParentId(parentId, pageable) : delegateTo.all(chain, pageable);
    }

    @Override
    public void deleteById(Queue<ID> chain, ID id) {
        ID parentId = chain.remove();
        if (!parentChildResolver.existsByParentIdAndId(parentId, id)) {
            throw new RelationshipNotFoundException();
        }
        delegateTo.deleteById(chain, id);
    }
}
