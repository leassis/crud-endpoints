package com.lassis.springframework.crud.configuration;

import lombok.Value;

import java.util.Set;

@Value
public class CRUDProperties {
    String basePath;
    Set<CRUDPathProperties> endpoints;
}
