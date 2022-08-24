package com.lassis.springframework.crud.util;

import com.lassis.springframework.crud.configuration.CRUDEndpointConfig;
import com.lassis.springframework.crud.configuration.CRUDEndpointsConfig;
import com.lassis.springframework.crud.entity.WithId;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@UtilityClass
@Slf4j
public class EndpointsUtil {

    private static final String CRUD_PROPERTY_PREFIX = "crud";
    private static final String CRUD_ENDPOINTS_PROPERTY_PREFIX = CRUD_PROPERTY_PREFIX + ".endpoints";
    private static final int TOKEN_COUNT = 3;

    public CRUDEndpointsConfig getConfig() {
        ClassPathResource resource = new ClassPathResource("endpoints.yaml");
        log.debug("loading file: {}", resource);

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource);

        Properties properties = yaml.getObject();
        log.debug("properties loaded");
        long count = properties.stringPropertyNames()
                .stream()
                .filter(v -> v.contains(CRUD_ENDPOINTS_PROPERTY_PREFIX))
                .count();

        Set<CRUDEndpointConfig> endpoints = new HashSet<>();
        for (long i = 0; i < count / TOKEN_COUNT; i++) {
            String propertyPrefix = CRUD_ENDPOINTS_PROPERTY_PREFIX + "[" + i + "]";
            String path = properties.getProperty(propertyPrefix + ".path");
            String entityClass = properties.getProperty(propertyPrefix + ".entity-class");
            String dtoClass = properties.getProperty(propertyPrefix + ".dto-class");
            String idClass = properties.getProperty(propertyPrefix + ".id-class");

            if (dtoClass == null) {
                dtoClass = entityClass;
            }

            try {
                endpoints.add(new CRUDEndpointConfig(
                        path,
                        (Class<? extends WithId<? extends Serializable>>) Class.forName(entityClass).asSubclass(WithId.class),
                        Class.forName(idClass).asSubclass(Serializable.class),
                        Class.forName(dtoClass).asSubclass(Serializable.class)
                ));
            } catch (Exception e) {
                throw new RuntimeException("cannot load conf for path " + path, e);
            }
        }

        return new CRUDEndpointsConfig(properties.getProperty(CRUD_PROPERTY_PREFIX + ".base-path"), endpoints);
    }

}
