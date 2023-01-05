package com.lassis.springframework.crud.util;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lassis.springframework.crud.configuration.CRUDProperties;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class EndpointsUtil {

    @NonNull
    public CRUDProperties getConfig(@NonNull Resource resource) {
        log.debug("loading file: {}", resource);

        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();

        try {
            CRUDProperties crud = mapper.readValue(resource.getInputStream(), CRUDProperties.class);
            return crud;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CRUDProperties getConfig() {
        return getConfig(new ClassPathResource("endpoints.yaml"));
    }

}
