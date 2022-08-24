package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.DtoConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
class CRUDAPIConfiguration {

    @Bean
    public RouterFunction<ServerResponse> crudRouterFunction(ApplicationContext context, CRUDEndpointsConfig config) {
        RouterFunctions.Builder route = route();

        for (CRUDEndpointConfig endpoint : config.getEndpoints()) {
            String path = config.getBasePath() + endpoint.getPath();

            Class<? extends WithId<? extends Serializable>> entityClass = endpoint.getEntityClass();
            Class<? extends Serializable> dtoClass = endpoint.getDtoClass();
            Class<? extends Serializable> idClass = endpoint.getIdClass();

            CrudService<WithId<Serializable>, Serializable> service = resolve(forClassWithGenerics(CrudService.class, entityClass, idClass), context);
            IdMapper<Serializable> idMapper = resolve(forClassWithGenerics(IdMapper.class, idClass), context);
            DtoConverter<Serializable, WithId<Serializable>> dtoConverter = resolve(forClassWithGenerics(DtoConverter.class, entityClass, dtoClass), context);

            route = route.nest(path(path), builder ->
                    builder.GET("", req -> ok().body(retrieve(service, dtoConverter)))
                            .GET("/{id}", req -> ok().body(retrieve(service, idMapper, dtoConverter, req)))
                            .POST("", req -> ok().body(create(dtoClass, service, dtoConverter, req)))
                            .PUT("/{id}", req -> ok().body(update(dtoClass, service, idMapper, dtoConverter, req)))
                            .DELETE("/{id}", req -> delete(service, idMapper, req)));
            log.info("crud endpoint {} was created", path);
        }

        return route.build();
    }

    @Bean
    public IdMapper<Long> longIdMapper() {
        return Long::valueOf;
    }

    @Bean
    public IdMapper<Integer> integerIdMapper() {
        return Integer::valueOf;
    }

    @Bean
    public IdMapper<UUID> uuidIdMapper() {
        return UUID::fromString;
    }

    private Serializable create(Class<? extends Serializable> dtoClass,
                                CrudService<WithId<Serializable>, Serializable> service,
                                DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                ServerRequest req) throws javax.servlet.ServletException, java.io.IOException {

        return dtoConverter.toDto(service.create(getBody(dtoConverter, dtoClass, req)));
    }

    private List<Serializable> retrieve(CrudService<WithId<Serializable>,
            Serializable> service,
                                        DtoConverter<Serializable, WithId<Serializable>> dtoConverter) {

        return service.findAll(null).map(dtoConverter::toDto).getContent();
    }

    private Serializable retrieve(CrudService<WithId<Serializable>, Serializable> service,
                                  IdMapper<Serializable> idMapper,
                                  DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                  ServerRequest req) {

        return dtoConverter.toDto(service.get(idMapper.apply(req.pathVariable("id"))));
    }

    private Serializable update(Class<? extends Serializable> dtoClass,
                                CrudService<WithId<Serializable>, Serializable> service,
                                IdMapper<Serializable> idMapper,
                                DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                ServerRequest req) throws javax.servlet.ServletException, java.io.IOException {

        return dtoConverter.toDto(service
                .update(idMapper.apply(req.pathVariable("id")), getBody(dtoConverter, dtoClass, req)));
    }

    private ServerResponse delete(CrudService<WithId<Serializable>,
            Serializable> service,
                                  IdMapper<Serializable> idMapper,
                                  ServerRequest req) {

        service.deleteById(idMapper.apply(req.pathVariable("id")));
        return ServerResponse.noContent().build();
    }

    private WithId<Serializable> getBody(
            DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
            Class<? extends Serializable> clazz, ServerRequest req)
            throws javax.servlet.ServletException, java.io.IOException {

        return dtoConverter.fromDto(req.body(clazz));
    }

    private <R> R resolve(ResolvableType type, ApplicationContext context) {
        ObjectProvider<R> beanProvider = context.getBeanProvider(type);
        return beanProvider.getObject();
    }
}

