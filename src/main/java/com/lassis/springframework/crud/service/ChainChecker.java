package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;

import java.io.Serializable;
import java.util.Stack;

public interface ChainChecker<E extends WithId<ID>, ID extends Serializable> {

    boolean exists(Stack<ID> chain, ID id);

    boolean exists(Stack<ID> chain);
}
