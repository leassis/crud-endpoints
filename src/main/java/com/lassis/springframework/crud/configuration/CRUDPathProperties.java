package com.lassis.springframework.crud.configuration;

import java.io.Serializable;
import java.util.Set;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lassis.springframework.crud.entity.WithId;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CRUDPathProperties {
    String path;

    Set<HttpMethod> methods;

    @JsonProperty("entity-class")
    Class<? extends WithId<? extends Serializable>> entityClass;

    @JsonProperty("dto-class")
    Class<? extends Serializable> dtoClass;

    @JsonProperty("page-size")
    int pageSize;
    
    CRUDPathProperties parent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonProperty("sub")
    Set<CRUDPathProperties> endpoints;
}
