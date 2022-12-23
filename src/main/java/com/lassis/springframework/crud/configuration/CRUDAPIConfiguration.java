package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.pojo.Pagination;
import com.lassis.springframework.crud.pojo.Result;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.DtoConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.notFound;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
class CRUDAPIConfiguration {
    private final static Pattern PAGE_PATTERN = Pattern.compile("^(P|F)\\d+S\\d$");

    @Bean
    public RouterFunction<ServerResponse> crudRouterFunction(ApplicationContext context, CRUDProperties config) {
        RouterFunctions.Builder route = route();

        Predicate<ServerRequest> rootPredicate = req -> true;
        for (CRUDPathProperties endpoint : config.getEndpoints()) {
            String path = config.getBasePath() + endpoint.getPath();
            route = createRoute(context, route, endpoint, path, 0, rootPredicate);
        }

        return route.build();
    }

    private RouterFunctions.Builder createRoute(ApplicationContext context,
                                                RouterFunctions.Builder route,
                                                CRUDPathProperties endpoint,
                                                String path,
                                                Integer level,
                                                Predicate<ServerRequest> validator) {

        final String pathVarName = "id" + level;
        final String pathVar = "/{" + pathVarName + "}";

        final Class<? extends WithId<? extends Serializable>> entityClass = endpoint.getEntityClass();
        final Class<? extends Serializable> dtoClass = endpoint.getDtoClass();
        final Class<? extends Serializable> idClass = endpoint.getIdClass();

        final CrudService<WithId<Serializable>, Serializable> service = resolve(forClassWithGenerics(CrudService.class, entityClass, idClass), context);
        final IdMapper<Serializable> idMapper = resolve(forClassWithGenerics(IdMapper.class, idClass), context);
        final DtoConverter<Serializable, WithId<Serializable>> dtoConverter;
        if (dtoClass != null && entityClass != dtoClass) {
            dtoConverter = resolve(forClassWithGenerics(DtoConverter.class, entityClass, dtoClass), context);
        } else {
            dtoConverter = bypassDtoConverter();
        }

        route = route.nest(path(path), builder -> {
            if (endpoint.getMethods().contains(HttpMethod.GET)) {
                builder.GET("", req -> ifValid(req, validator, () -> retrieve(req, service, dtoConverter, endpoint)))
                        .GET(pathVar, req -> ifValid(req, validator, () -> retrieveById(service, idMapper, dtoConverter, req, level)));
            }

            if (endpoint.getMethods().contains(HttpMethod.POST)) {
                builder.POST("", req -> ifValid(req, validator, () -> create(dtoClass, service, dtoConverter, req)));
            }

            if (endpoint.getMethods().contains(HttpMethod.PUT)) {
                builder.PUT(pathVar, req -> ifValid(req, validator, () -> update(dtoClass, service, idMapper, dtoConverter, req, level)));
            }

            if (endpoint.getMethods().contains(HttpMethod.DELETE)) {
                builder.DELETE(pathVar, req -> validator.test(req) ? delete(service, idMapper, req, level) : notFound().build());
            }
        });
        log.info("crud endpoint {} was created", path);

        for (CRUDPathProperties sub : endpoint.getSubPaths()) {
            Predicate<ServerRequest> valid = req ->
                    service.exists(idMapper.apply(req.pathVariable(pathVarName)));

            String subPath = path + pathVar + sub.getPath();

            route = createRoute(context, route, sub, subPath, level + 1, valid);
        }

        return route;
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

    private ServerResponse ifValid(ServerRequest req, Predicate<ServerRequest> validator, ResultSupplier handler) throws Exception {
        return validator.test(req) ? ok().body(handler.get()) : notFound().build();
    }

    private Result<Serializable, ?> create(Class<? extends Serializable> dtoClass,
                                           CrudService<WithId<Serializable>, Serializable> service,
                                           DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                           ServerRequest req) throws javax.servlet.ServletException, java.io.IOException {

        Serializable data = dtoConverter.toDto(service.create(getBody(dtoConverter, dtoClass, req)));
        return Result.of(data);
    }

    private Result<List<Serializable>, Pagination> retrieve(ServerRequest req,
                                                            CrudService<WithId<Serializable>, Serializable> service,
                                                            DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                                            CRUDPathProperties crudPathProperties) {

        Pageable pageable = getPageable(req, crudPathProperties.getPageSize());
        Page<Serializable> pageContent = service.findAll(pageable)
                .map(dtoConverter::toDto);

        return Result.of(pageContent.getContent(), toPagination(pageContent));
    }


    private Result<Serializable, ?> retrieveById(CrudService<WithId<Serializable>, Serializable> service,
                                                 IdMapper<Serializable> idMapper,
                                                 DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                                 ServerRequest req,
                                                 int level) {

        Serializable id = idMapper.apply(req.pathVariable("id" + level));
        Serializable data = dtoConverter.toDto(service.get(id));
        return Result.of(data);
    }

    private Result<Serializable, ?> update(Class<? extends Serializable> dtoClass,
                                           CrudService<WithId<Serializable>, Serializable> service,
                                           IdMapper<Serializable> idMapper,
                                           DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                           ServerRequest req, int level) throws javax.servlet.ServletException, java.io.IOException {

        Serializable id = idMapper.apply(req.pathVariable("id" + level));
        WithId<Serializable> body = getBody(dtoConverter, dtoClass, req);
        Serializable data = dtoConverter.toDto(service.update(id, body));
        return Result.of(data);
    }

    private ServerResponse delete(CrudService<WithId<Serializable>, Serializable> service,
                                  IdMapper<Serializable> idMapper,
                                  ServerRequest req,
                                  int level) {

        Serializable id = idMapper.apply(req.pathVariable("id" + level));
        service.deleteById(id);
        return ServerResponse.noContent().build();
    }


    private Pagination toPagination(Page<Serializable> pageContent) {
        Pageable pageable = pageContent.getPageable();
        Pageable first = pageable.first();
        String tFirst = "F" + first.getPageNumber() + "S" + first.getPageSize();

        String tPrev = null;
        if (pageContent.hasPrevious()) {
            Pageable prev = pageable.previousOrFirst();

            tPrev = prev.getPageNumber() == first.getPageNumber()
                    ? tFirst
                    : "P" + prev.getPageNumber() + "S" + prev.getPageSize();
        }

        String tNext = null;
        if (pageContent.hasNext()) {
            Pageable next = pageable.next();
            tNext = "P" + next.getPageNumber() + "S" + next.getPageSize();
        }
        return Pagination.of(tFirst, tPrev, tNext);
    }

    private Pageable getPageable(ServerRequest req, int size) {
        String page = req.param("page").orElseGet(() -> req.headers().firstHeader("page"));
        if (Objects.isNull(page) || !PAGE_PATTERN.matcher(page).matches()) {
            Integer pageSize = req.param("size").map(Integer::parseInt).orElse(size);
            return PageRequest.of(0, pageSize, Sort.by("id"));
        }

        int indexOfS = page.indexOf("S");
        int pageNumber = Integer.parseInt(page.substring(1, indexOfS));
        int pageSize = Integer.parseInt(page.substring(indexOfS + 1));
        return PageRequest.of(pageNumber, pageSize, Sort.by("id"));
    }


    private WithId<Serializable> getBody(DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                         Class<? extends Serializable> clazz, ServerRequest req)
            throws javax.servlet.ServletException, java.io.IOException {

        Serializable body = req.body(clazz);
        return dtoConverter.fromDto(body);
    }

    private <R> R resolve(ResolvableType type, ApplicationContext context) {
        ObjectProvider<R> beanProvider = context.getBeanProvider(type);
        return beanProvider.getObject();
    }

    private static DtoConverter<Serializable, WithId<Serializable>> bypassDtoConverter() {
        return new DtoConverter<Serializable, WithId<Serializable>>() {
            @Override
            public WithId<Serializable> fromDto(Serializable obj) {
                return (WithId<Serializable>) obj;
            }

            @Override
            public Serializable toDto(WithId<Serializable> entity) {
                return entity;
            }
        };
    }

    private interface ResultSupplier {

        Result<?, ?> get() throws Exception;
    }
}

