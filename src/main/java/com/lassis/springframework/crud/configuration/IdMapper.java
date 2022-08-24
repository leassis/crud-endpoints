package com.lassis.springframework.crud.configuration;

import java.io.Serializable;
import java.util.function.Function;

public interface IdMapper<O extends Serializable> extends Function<String, O> {}

