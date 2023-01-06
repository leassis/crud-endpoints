package com.lassis.springframework.crud.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.pojo.DtoType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
public class CRUDPathProperties {
    String path;

    Set<HttpMethod> methods;

    @JsonProperty("entity-class")
    Class<? extends WithId<? extends Serializable>> entityClass;

    @JsonProperty("dto-class")
    Class<? extends Serializable> dtoClass;

    @JsonProperty("dto-classes")
    Map<DtoType, Class<? extends Serializable>> dtoClasses;

    @JsonProperty("page-size")
    int pageSize;

    @Setter
    CRUDPathProperties parent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonProperty("endpoints")
    @Setter
    Set<CRUDPathProperties> endpoints;

    public final Class<? extends Serializable> getDtoClass() {
        return Objects.nonNull(dtoClass) ? dtoClass : entityClass;
    }

    public final Class<? extends Serializable> getDtoClass(DtoType dtoType) {

        return Objects.nonNull(dtoClasses)
                ? dtoClasses.getOrDefault(dtoType, getDtoClass())
                : getDtoClass();
    }
}
