package com.lassis.springframework.crud.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRUDProperties {
    @JsonProperty("base-path")
    String basePath;

    @JsonProperty("id-class")
    Class<? extends Serializable> idClass;

    @JsonProperty("page-size")
    Integer pageSize;

    Set<HttpMethod> methods;

    Set<CRUDPathProperties> endpoints;

}
