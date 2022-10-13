package com.lassis.springframework.crud.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value(staticConstructor = "of")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T, M> {
    T data;
    M meta;

    public static <V> Result<V, ?> of(V data) {
        return of(data, null);
    }
}
