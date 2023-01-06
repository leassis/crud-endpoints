package com.lassis.springframework.crud.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

@Getter
public class CRUDProperties {
    @JsonProperty("base-path")
    String basePath;

    @JsonProperty("id-class")
    Class<? extends Serializable> idClass;

    Set<CRUDPathProperties> endpoints;

}
