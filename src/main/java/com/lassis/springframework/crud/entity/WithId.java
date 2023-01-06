package com.lassis.springframework.crud.entity;

import java.io.Serializable;

public interface WithId<I extends Serializable> extends Serializable {
    I getId();
}
