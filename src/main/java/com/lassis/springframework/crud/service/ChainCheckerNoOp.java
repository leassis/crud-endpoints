package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.util.Collection;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class ChainCheckerNoOp<E extends WithId<ID>, ID extends Serializable> implements ChainChecker<E, ID> {
    private final ResolvableType chainCheckerType;

    @Override
    public boolean exists(Stack<ID> chain, ID id) {
        failIfMultiLevel(chain);
        return true;
    }

    @Override
    public boolean exists(Stack<ID> chain) {
        failIfMultiLevel(chain);
        return true;
    }

    private void failIfMultiLevel(Collection<ID> chain) {
        if (!chain.isEmpty()) {
            log.error("{} can only be used with single level endpoints, define a {}", this.getClass(), chainCheckerType);
            throw new IllegalStateException(this.getClass() + " can only be used with single level endpoints, define a " + chainCheckerType);
        }
    }
}
