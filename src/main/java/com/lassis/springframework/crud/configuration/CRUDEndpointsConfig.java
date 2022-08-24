package com.lassis.springframework.crud.configuration;

import lombok.Value;

import java.util.Set;

@Value
public class CRUDEndpointsConfig {
    String basePath;
    Set<CRUDEndpointConfig> endpoints;
}
