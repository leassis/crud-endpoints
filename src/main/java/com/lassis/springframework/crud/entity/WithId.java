package com.lassis.springframework.crud.entity;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public interface WithId<ID extends Serializable> extends Serializable {
    @NonNull
    ID getId();
}
