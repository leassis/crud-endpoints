package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class CRUDPathProperties {
    private final String path;
    private final Set<HttpMethod> methods;
    private final Class<? extends WithId<? extends Serializable>> entityClass;
    private final Class<? extends Serializable> idClass;
    private final Class<? extends Serializable> dtoClass;
    private final int pageSize;
    private final CRUDPathProperties parent;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Set<CRUDPathProperties> subPaths;
}
