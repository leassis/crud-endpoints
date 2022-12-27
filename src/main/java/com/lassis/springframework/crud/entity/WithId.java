package com.lassis.springframework.crud.entity;

import java.io.Serializable;

public interface WithId<ID extends Serializable> extends Serializable {
    ID getId();
}
