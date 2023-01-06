package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.exception.ValidationException;
import com.lassis.springframework.crud.pojo.BodyValidation;
import com.lassis.springframework.crud.pojo.BodyValidation.BodyContent;
import com.lassis.springframework.crud.pojo.DtoType;
import com.lassis.springframework.crud.pojo.Pagination;
import com.lassis.springframework.crud.pojo.Result;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.DtoConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Slf4j
@RequiredArgsConstructor
class CRUDAPIConfiguration {
    private static final Pattern PAGE_PATTERN = Pattern.compile("^[PF]\\d+S\\d+$");
    private static final DtoConverter<Serializable, Serializable, WithId<Serializable>> BYPASS_DTO_CONVERTER = bypassDtoConverter();
    private final ApplicationContext context;
    private final CRUDProperties config;

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
    @ConditionalOnMissingBean(Validator.class)
    Validator validator() {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator();
        }
    }

    @Bean
    RouterFunction<ServerResponse> crudRouterFunction() {
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
        final Class<? extends Serializable> idClass = config.getIdClass();

        final CrudService<WithId<Serializable>, Serializable> service = resolve(forClassWithGenerics(CrudService.class, entityClass, idClass), context);
        final IdMapper<Serializable> idMapper = resolve(forClassWithGenerics(IdMapper.class, idClass), context);

        route = route.nest(path(path), builder -> {
            if (endpoint.getMethods().contains(HttpMethod.GET)) {
                builder.GET("", req -> retrieve(req, service, idMapper, endpoint, level))
                        .GET(pathVar, req -> retrieveById(req, service, endpoint, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.POST)) {
                builder.POST("", req -> create(req, service, endpoint, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.PUT)) {
                builder.PUT(pathVar, req -> update(req, service, endpoint, idMapper, level));
            }

            if (endpoint.getMethods().contains(HttpMethod.DELETE)) {
                builder.DELETE(pathVar, req -> delete(req, service, idMapper, level));
            }
        });
        log.info("crud endpoint {} was created", path);

        for (CRUDPathProperties sub : endpoint.getEndpoints()) {
            String subPath = path + pathVar + sub.getPath();

            route = createRoute(context, route, sub, subPath, level + 1);
        }

        return route;
    }


    private ServerResponse create(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  CRUDPathProperties endpoint,
                                  IdMapper<Serializable> idMapper,
                                  int level)
            throws javax.servlet.ServletException, java.io.IOException {

        Queue<Serializable> idChain = toIdChain(idMapper, req, level);

        try {
            DtoConverter<Serializable, Serializable, WithId<Serializable>> dtoConverter = getDtoConverter(endpoint, DtoType.POST, DtoType.RESULT);
            WithId<Serializable> body = getBody(dtoConverter, endpoint.getDtoClass(DtoType.POST), req);
            WithId<Serializable> created = service.create(idChain, body);

            Serializable data = dtoConverter.toDto(created);

            return ServerResponse.created(req.uri().resolve("/" + created.getId())).body(Result.of(data));
        } catch (ValidationException e) {
            return processValidationException(e);
        }

    }

    private ServerResponse retrieve(ServerRequest req,
                                    CrudService<WithId<Serializable>, Serializable> service,
                                    IdMapper<Serializable> idMapper,
                                    CRUDPathProperties endpoint,
                                    int level) {

        Queue<Serializable> idChain = toIdChain(idMapper, req, level);
        Pageable pageable = getPageable(req, endpoint.getPageSize());

        Page<Serializable> pageContent = service.all(idChain, pageable)
                .map(getDtoConverter(endpoint, DtoType.LIST, DtoType.LIST)::toDto);

        return ServerResponse.ok().body(Result.of(pageContent.getContent(), toPagination(pageContent)));
    }


    private ServerResponse retrieveById(ServerRequest req,
                                        CrudService<WithId<Serializable>, Serializable> service,
                                        CRUDPathProperties endpoint,
                                        IdMapper<Serializable> idMapper,
                                        int level) {

        Queue<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        Serializable data = getDtoConverter(endpoint, DtoType.GET, DtoType.GET).toDto(service.get(idChain, id));
        return ServerResponse.ok().body(Result.of(data));
    }

    private ServerResponse update(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  CRUDPathProperties endpoint, IdMapper<Serializable> idMapper,
                                  int level)
            throws javax.servlet.ServletException, java.io.IOException {

        Queue<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        try {
            DtoConverter<Serializable, Serializable, WithId<Serializable>> dtoConverter = getDtoConverter(endpoint, DtoType.PUT, DtoType.RESULT);
            WithId<Serializable> body = getBody(dtoConverter, endpoint.getDtoClass(DtoType.PUT), req);

            Serializable data = dtoConverter.toDto(service.update(idChain, id, body));

            return ServerResponse.ok().body(Result.of(data));
        } catch (ValidationException e) {
            return processValidationException(e);
        }
    }

    private ServerResponse delete(ServerRequest req,
                                  CrudService<WithId<Serializable>, Serializable> service,
                                  IdMapper<Serializable> idMapper,
                                  int level) {

        Queue<Serializable> idChain = toIdChain(idMapper, req, level);

        Serializable id = idMapper.apply(req.pathVariable("id" + level));

        service.deleteById(idChain, id);
        return ServerResponse.noContent().build();
    }


    private static ServerResponse processValidationException(ValidationException e) {
        BodyValidation bodyValidation = e.getErrors()
                .stream()
                .map(v -> new BodyContent(v.getPropertyPath().toString(), v.getMessage()))
                .collect(collectingAndThen(Collectors.toSet(), BodyValidation::new));
        return ServerResponse.badRequest().body(bodyValidation);
    }

    private static Queue<Serializable> toIdChain(IdMapper<Serializable> idMapper, ServerRequest req, int level) {
        return IntStream.range(0, level).boxed()
                .map(i -> "id" + i)
                .map(req::pathVariable)
                .map(idMapper)
                .collect(Collectors.toCollection(LinkedList::new));
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


    private WithId<Serializable> getBody(DtoConverter<Serializable, Serializable, WithId<Serializable>> dtoConverter,
                                         Class<? extends Serializable> clazz, ServerRequest req)
            throws javax.servlet.ServletException, java.io.IOException, ValidationException {

        Serializable body = req.body(clazz);

        Set<ConstraintViolation<Serializable>> errors = validator().validate(body);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

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

    private DtoConverter<Serializable, Serializable, WithId<Serializable>> getDtoConverter(CRUDPathProperties endpoint,
                                                                                           DtoType inputDtoType,
                                                                                           DtoType outputDtoType) {

        final Class<? extends Serializable> inputDtoClass = endpoint.getDtoClass(inputDtoType);
        final Class<? extends Serializable> outputDtoClass = endpoint.getDtoClass(outputDtoType);
        final Class<? extends WithId<? extends Serializable>> entityClass = endpoint.getEntityClass();
        return resolve(forClassWithGenerics(DtoConverter.class, inputDtoClass, outputDtoClass, entityClass), context, () -> BYPASS_DTO_CONVERTER);
    }

    private static DtoConverter<Serializable, Serializable, WithId<Serializable>> bypassDtoConverter() {
        return new DtoConverter<Serializable, Serializable, WithId<Serializable>>() {
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
