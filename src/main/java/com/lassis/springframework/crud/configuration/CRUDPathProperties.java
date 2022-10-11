package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import lombok.Value;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Set;

@Value
public class CRUDPathProperties {
    String path;
    Set<HttpMethod> methods;
    Class<? extends WithId<? extends Serializable>> entityClass;
    Class<? extends Serializable> idClass;
    Class<? extends Serializable> dtoClass;
}
