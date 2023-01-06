package com.lassis.springframework.crud.util;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lassis.springframework.crud.configuration.CRUDPathProperties;
import com.lassis.springframework.crud.configuration.CRUDProperties;
import com.lassis.springframework.crud.exception.ReadEndpointFileException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@UtilityClass
@Slf4j
public class EndpointsUtil {

    private static final Set<HttpMethod> ALLOWED_HTTP_METHODS = Stream.of(GET, POST, PUT, DELETE)
            .collect(collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

    @NonNull
    public CRUDProperties getConfig(@NonNull Resource resource) {
        log.debug("loading file: {}", resource);

        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();

        try {
            CRUDProperties crudProperties = mapper.readValue(resource.getInputStream(), CRUDProperties.class);
            configureEndpoints(crudProperties, crudProperties.getEndpoints(), null);
            return crudProperties;
        } catch (IOException e) {
            throw new ReadEndpointFileException(e);
        }
    }

    public CRUDProperties getConfig() {
        return getConfig(new ClassPathResource("endpoints.yaml"));
    }

    private void configureEndpoints(CRUDProperties config, Collection<CRUDPathProperties> endpoints, CRUDPathProperties parent) {
        if (endpoints == null || endpoints.isEmpty()) {
            return;
        }

        for (CRUDPathProperties endpoint : endpoints) {
            endpoint.setParent(parent);

            nonNullEndpointsCollection(endpoint);
            useDefaultMethodsIfNeeded(config, endpoint);
            useDefaultPaginationIfNeeded(config, endpoint);

            configureEndpoints(config, endpoint.getEndpoints(), endpoint);
        }
    }

    private void useDefaultPaginationIfNeeded(CRUDProperties config, CRUDPathProperties endpoint) {
        if (isNull(endpoint.getPageSize())) {
            endpoint.setPageSize(config.getPageSize());
        }
    }

    private void useDefaultMethodsIfNeeded(CRUDProperties config, CRUDPathProperties endpoint) {
        Set<HttpMethod> methods = endpoint.getMethods();
        if (nonNull(methods) && !methods.isEmpty()) {
            return;
        }

        methods = config.getMethods();
        if (isNull(methods) || methods.isEmpty()) {
            methods = ALLOWED_HTTP_METHODS;
        }
        endpoint.setMethods(methods);
    }

    private void nonNullEndpointsCollection(CRUDPathProperties endpoint) {
        if (isNull(endpoint.getEndpoints())) {
            endpoint.setEndpoints(Collections.emptySet());
        }
    }
}
