package com.lassis.springframework.crud.fake;

import com.lassis.springframework.crud.entity.WithId;

public class Clz4 implements WithId<Long> {
    @Override
    public Long getId() {
        return null;
    }
}
