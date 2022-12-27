package com.lassis.springframework.crud.configuration;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.function.Function;


public interface IdMapper<O extends Serializable> extends Function<String, O> {
    @Override
    @NonNull
    O apply(@NonNull String s);
}
