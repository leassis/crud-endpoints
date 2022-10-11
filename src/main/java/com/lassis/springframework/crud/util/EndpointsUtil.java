package com.lassis.springframework.crud.util;

import com.lassis.springframework.crud.configuration.CRUDPathProperties;
import com.lassis.springframework.crud.configuration.CRUDProperties;
import com.lassis.springframework.crud.entity.WithId;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@UtilityClass
@Slf4j
public class EndpointsUtil {

    private static final String CRUD_PROPERTY_PREFIX = "crud";
    private static final String CRUD_ENDPOINTS_PROPERTY_PREFIX = CRUD_PROPERTY_PREFIX + ".endpoints";

    private static final int TOKEN_COUNT = 3;
    private static final Set<HttpMethod> HTTP_METHODS = Arrays.stream(HttpMethod.values()).collect(Collectors.toSet());

    public CRUDProperties getConfig() {
        ClassPathResource resource = new ClassPathResource("endpoints.yaml");
        log.debug("loading file: {}", resource);

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource);

        Properties properties = yaml.getObject();
        log.debug("properties loaded");

        return new CRUDProperties(
                properties.getProperty(CRUD_PROPERTY_PREFIX + ".base-path"),
                endpoints(properties)
        );
    }

    private static Set<HttpMethod> methods(Properties properties, String rootProperty) {
        String baseProperty = rootProperty + ".methods";
        long count = properties.stringPropertyNames()
                .stream()
                .filter(v -> v.contains(baseProperty))
                .count();

        if (count == 0) {
            return HTTP_METHODS;
        }

        return LongStream.range(0, count)
                .mapToObj(i -> properties.getProperty(baseProperty + "[" + i + "]"))
                .map(String::toUpperCase)
                .map(HttpMethod::resolve)
                .collect(Collectors.toSet());
    }

    private static Set<CRUDPathProperties> endpoints(Properties properties) {
        Map<String, List<String>> map = properties.stringPropertyNames()
                .stream()
                .filter(v -> v.contains("endpoints"))
                .collect(Collectors.groupingBy(EndpointsUtil::endpointsPrefix));

        Set<CRUDPathProperties> endpoints = new HashSet<>();
        for (String propertyPrefix : map.keySet()) {
            String path = properties.getProperty(propertyPrefix + ".path");
            String entityClass = properties.getProperty(propertyPrefix + ".entity-class");
            String dtoClass = properties.getProperty(propertyPrefix + ".dto-class");
            String idClass = properties.getProperty(propertyPrefix + ".id-class");

            if (dtoClass == null) {
                dtoClass = entityClass;
            }

            try {
                endpoints.add(new CRUDPathProperties(
                        path,
                        methods(properties, propertyPrefix),
                        (Class<? extends WithId<? extends Serializable>>) Class.forName(entityClass).asSubclass(WithId.class),
                        Class.forName(idClass).asSubclass(Serializable.class),
                        Class.forName(dtoClass).asSubclass(Serializable.class)
                ));
            } catch (Exception e) {
                throw new RuntimeException("cannot load conf for path " + path, e);
            }
        }
        return endpoints;
    }

    private static String endpointsPrefix(String s) {
        return s.substring(0, s.indexOf(".", CRUD_PROPERTY_PREFIX.length() + 1));
    }

}
