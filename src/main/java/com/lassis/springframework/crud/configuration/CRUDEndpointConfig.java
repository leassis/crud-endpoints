package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import lombok.Value;

import java.io.Serializable;

@Value
public class CRUDEndpointConfig {
    String path;
    Class<? extends WithId<? extends Serializable>> entityClass;
    Class<? extends Serializable> idClass;
    Class<? extends Serializable> dtoClass;
}
