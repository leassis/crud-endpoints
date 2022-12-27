package com.lassis.springframework.crud.util;

import com.lassis.springframework.crud.configuration.CRUDPathProperties;
import com.lassis.springframework.crud.configuration.CRUDProperties;
import com.lassis.springframework.crud.entity.WithId;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@UtilityClass
@Slf4j
public class EndpointsUtil {

    public static final Pattern SUB_PATTERN = Pattern.compile("\\.sub\\[\\d+\\]");
    private static final String CRUD_PROPERTY_PREFIX = "crud";
    private static final Set<HttpMethod> HTTP_METHODS = Arrays.stream(HttpMethod.values()).collect(Collectors.toSet());

    @NonNull
    public CRUDProperties getConfig(@NonNull Resource resource) {
        log.debug("loading file: {}", resource);

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource);

        Properties properties = yaml.getObject();
        log.info("{} properties loaded", resource);

        Set<CRUDPathProperties> endpoints = endpoints(
                properties,
                properties.getProperty(CRUD_PROPERTY_PREFIX + ".id-class"),
                EndpointsUtil::containsEndpoint,
                EndpointsUtil::endpointsPrefix
        );

        return new CRUDProperties(
                properties.getProperty(CRUD_PROPERTY_PREFIX + ".base-path"),
                endpoints
        );
    }

    public CRUDProperties getConfig() {
        return getConfig(new ClassPathResource("endpoints.yaml"));
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

    private static Set<CRUDPathProperties> endpoints(Properties properties,
                                                     String idClass,
                                                     Predicate<String> filtering,
                                                     Function<String, String> grouping) {

        Map<String, List<String>> map = properties.stringPropertyNames()
                .stream()
                .filter(filtering)
                .collect(Collectors.groupingBy(grouping));

        Set<CRUDPathProperties> endpoints = new HashSet<>();
        for (String propertyPrefix : map.keySet()) {
            String path = properties.getProperty(propertyPrefix + ".path", "");
            String entityClass = properties.getProperty(propertyPrefix + ".entity-class");
            String dtoClass = properties.getProperty(propertyPrefix + ".dto-class", entityClass);
            String pageSize = properties.getProperty(propertyPrefix + ".page-size", "999999999");

            try {
                Predicate<String> containsSub = v -> v.startsWith(propertyPrefix) && v.indexOf("sub", propertyPrefix.length() + 1) > -1;

                CRUDPathProperties endpoint = new CRUDPathProperties(
                        path,
                        methods(properties, propertyPrefix),
                        (Class<? extends WithId<? extends Serializable>>) Class.forName(entityClass).asSubclass(WithId.class),
                        Class.forName(idClass).asSubclass(Serializable.class),
                        Class.forName(dtoClass).asSubclass(Serializable.class),
                        Integer.parseInt(pageSize),
                        endpoints(properties, idClass, containsSub, v -> subEndpointsPrefix(v, propertyPrefix))
                );

                endpoints.add(endpoint);
            } catch (Exception e) {
                throw new RuntimeException("cannot load conf for path " + path, e);
            }
        }
        return endpoints;
    }

    private static boolean containsEndpoint(String s) {
        return s.contains("endpoints");
    }

    private static String endpointsPrefix(String s) {
        return s.substring(0, s.indexOf(".", CRUD_PROPERTY_PREFIX.length() + 1));
    }

    private static String subEndpointsPrefix(String s, String prefix) {
        Matcher matcher = SUB_PATTERN.matcher(s.substring(prefix.length()));
        matcher.find();
        return prefix + matcher.group();
    }

}
