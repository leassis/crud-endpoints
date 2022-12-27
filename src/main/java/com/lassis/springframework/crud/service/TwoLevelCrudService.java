package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Stack;

@RequiredArgsConstructor
public class TwoLevelCrudService<P extends WithId<ID>, E extends WithId<ID>, ID extends Serializable> implements CrudService<E, ID> {
    private final CrudService<E, ID> delegateTo;
    private final TwoLevelRepository<P, E, ID> twoLevelRepository;

    @Override
    public E create(Stack<ID> chain, E obj) {
        ID productId = chain.pop();

        P entity = twoLevelRepository.findParentById(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        twoLevelRepository.setParent(entity, obj);

        return delegateTo.create(chain, obj);
    }

    @Override
    public E update(Stack<ID> chain, ID id, E obj) {
        ID parentId = chain.pop();

        if (!twoLevelRepository.existsByParentIdAndId(parentId, obj.getId())) {
            throw new NotFoundException(id);
        }

        return delegateTo.update(chain, id, obj);
    }

    @Override
    public E get(Stack<ID> chain, ID id) {
        ID productId = chain.pop();
        return twoLevelRepository.findByParentIdAndId(productId, id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @Override
    public Page<E> all(Stack<ID> chain, Pageable pageable) {
        ID parentId = chain.pop();
        if (!twoLevelRepository.existsByParentId(parentId)) {
            throw new NotFoundException(parentId);
        }

        return twoLevelRepository.findAllByParentId(parentId, pageable);
    }

    @Override
    public void deleteById(Stack<ID> chain, ID id) {
        ID parentId = chain.pop();
        twoLevelRepository.deleteByParentIdAndId(parentId, id);
    }
}
