package com.lassis.springframework.crud.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class CRUDProperties {
    @JsonProperty("base-path")
    String basePath;

    @JsonProperty("id-class")
    Class<? extends Serializable> idClass;

    Set<CRUDPathProperties> endpoints;

}
