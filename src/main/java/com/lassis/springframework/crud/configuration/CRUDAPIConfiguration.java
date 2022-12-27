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
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Slf4j
class CRUDAPIConfiguration {
    private static final Pattern PAGE_PATTERN = Pattern.compile("^(P|F)\\d+S\\d+$");
    private static final DtoConverter<Serializable, WithId<Serializable>> BYPASS_DTO_CONVERTER = bypassDtoConverter();

    @Bean
    IdMapper<Long> longIdMapper() {
        return Long::valueOf;
    }

    @Bean
    IdMapper<Integer> integerIdMapper() {
        return Integer::valueOf;
    }

    @Bean
    IdMapper<UUID> uuidIdMapper() {
        return UUID::fromString;
    }

    @Bean
    RouterFunction<ServerResponse> crudRouterFunction(ApplicationContext context, CRUDProperties config) {
        RouterFunctions.Builder route = route();

        for (CRUDPathProperties endpoint : config.getEndpoints()) {
            String path = config.getBasePath() + endpoint.getPath();
            route = createRoute(context, route, endpoint, path, 0);
        }

        return route.build();
    }

    private RouterFunctions.Builder createRoute(ApplicationContext context,
                                                RouterFunctions.Builder route,
                                                CRUDPathProperties endpoint,
                                                String path,
                                                Integer level) {

        final String pathVarName = "id" + level;
        final String pathVar = "/{" + pathVarName + "}";

        final Class<? extends WithId<? extends Serializable>> entityClass = endpoint.getEntityClass();
        final Class<? extends Serializable> dtoClass = endpoint.getDtoClass();
        final Class<? extends Serializable> idClass = endpoint.getIdClass();

        final CrudService<WithId<Serializable>, Serializable> service = resolve(forClassWithGenerics(CrudService.class, entityClass, idClass), context);
        final IdMapper<Serializable> idMapper = resolve(forClassWithGenerics(IdMapper.class, idClass), context);
        final DtoConverter<Serializable, WithId<Serializable>> dtoConverter = resolve(forClassWithGenerics(DtoConverter.class, dtoClass, entityClass), context, () -> BYPASS_DTO_CONVERTER);

        route = route.nest(path(path), builder -> {
            if (endpoint.getMethods().contains(HttpMethod.GET)) {
                builder.GET("", req -> retrieve(req, service, dtoConverter, idMapper, endpoint, level))
                        .GET(pathVar, req -> retrieveById(req, service, dtoConverter, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.POST)) {
                builder.POST("", req -> create(req, service, dtoConverter, dtoClass, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.PUT)) {
                builder.PUT(pathVar, req -> update(req, service, dtoClass, dtoConverter, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.DELETE)) {
                builder.DELETE(pathVar, req -> delete(req, service, idMapper, level));
            }
        });
        log.info("crud endpoint {} was created", path);

        for (CRUDPathProperties sub : endpoint.getSubPaths()) {
            String subPath = path + pathVar + sub.getPath();

            route = createRoute(context, route, sub, subPath, level + 1);
        }

        return route;
    }


    private ServerResponse create(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                  Class<? extends Serializable> dtoClass,
                                  IdMapper<Serializable> idMapper,
                                  int level)
            throws javax.servlet.ServletException, java.io.IOException {

        Stack<Serializable> idChain = toIdChain(idMapper, req, level);

        WithId<Serializable> body = getBody(dtoConverter, dtoClass, req);
        Serializable data = dtoConverter.toDto(service.create(idChain, body));
        return ServerResponse.ok().body(Result.of(data));
    }

    private ServerResponse retrieve(ServerRequest req,
                                    CrudService<WithId<Serializable>, Serializable> service,
                                    DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                    IdMapper<Serializable> idMapper,
                                    CRUDPathProperties crudPathProperties,
                                    int level) {

        Stack<Serializable> idChain = toIdChain(idMapper, req, level);

        Pageable pageable = getPageable(req, crudPathProperties.getPageSize());
        Page<Serializable> pageContent = service.all(idChain, pageable)
                .map(dtoConverter::toDto);

        return ServerResponse.ok().body(Result.of(pageContent.getContent(), toPagination(pageContent)));
    }


    private ServerResponse retrieveById(ServerRequest req,
                                        CrudService<WithId<Serializable>, Serializable> service,
                                        DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                        IdMapper<Serializable> idMapper,
                                        int level) {

        Stack<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        Serializable data = dtoConverter.toDto(service.get(idChain, id));
        return ServerResponse.ok().body(Result.of(data));
    }

    private ServerResponse update(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  Class<? extends Serializable> dtoClass,
                                  DtoConverter<Serializable, WithId<Serializable>> dtoConverter,
                                  IdMapper<Serializable> idMapper,
                                  int level)
            throws javax.servlet.ServletException, java.io.IOException {

        Stack<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        WithId<Serializable> body = getBody(dtoConverter, dtoClass, req);

        Serializable data = dtoConverter.toDto(service.update(idChain, id, body));
        return ServerResponse.ok().body(Result.of(data));
    }

    private ServerResponse delete(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  IdMapper<Serializable> idMapper,
                                  int level) {

        Stack<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        service.deleteById(idChain, id);
        return ServerResponse.noContent().build();
    }


    private static Stack<Serializable> toIdChain(IdMapper<Serializable> idMapper, ServerRequest req, int level) {
        return IntStream.range(0, level).boxed()
                .map(i -> "id" + i)
                .map(req::pathVariable)
                .map(idMapper)
                .collect(Collectors.toCollection(Stack::new));
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

    private <R> R resolve(ResolvableType type, ApplicationContext context, Supplier<R> defaultBean) {
        ObjectProvider<R> beanProvider = context.getBeanProvider(type);
        return beanProvider.getIfAvailable(defaultBean);
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

}
