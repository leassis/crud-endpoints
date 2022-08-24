package com.lassis.springframework.crud.entity;

import java.io.Serializable;

public interface ProjectionId<ID extends Serializable> extends WithId<ID> {

    @Override
    ID getId();
}
