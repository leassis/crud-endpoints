package com.lassis.springframework.crud.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
// @RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class CRUDProperties {
    @JsonProperty("base-path")
    String basePath;
  
    @JsonProperty("id-class")
    Class<? extends Serializable> idClass;
  
    Set<CRUDPathProperties> endpoints;
}
