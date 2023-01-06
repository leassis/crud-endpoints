package com.lassis.springframework.crud.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.pojo.DtoType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRUDPathProperties {
    String path;

    Set<HttpMethod> methods;

    @JsonProperty("entity-class")
    Class<? extends WithId<? extends Serializable>> entityClass;

    @JsonProperty("dto-class")
    @Getter(AccessLevel.PRIVATE)
    Class<? extends Serializable> dtoClass;

    @JsonProperty("dto-classes")
    @Getter(AccessLevel.PRIVATE)
    Map<DtoType, Class<? extends Serializable>> dtoClasses;

    @JsonProperty("page-size")
    Integer pageSize;

    @Setter
    CRUDPathProperties parent;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonProperty("endpoints")
    @Setter
    Set<CRUDPathProperties> endpoints;

    private Class<? extends Serializable> getDtoClass() {
        return Objects.nonNull(dtoClass) ? dtoClass : entityClass;
    }

    public final Class<? extends Serializable> getDtoClass(DtoType dtoType) {
        return Objects.nonNull(dtoClasses)
                ? dtoClasses.getOrDefault(dtoType, getDtoClass())
                : getDtoClass();
    }

}
