package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.service.BeforeSave;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.GenericUpdateValuesSetter;
import com.lassis.springframework.crud.service.MultiLevelCrudService;
import com.lassis.springframework.crud.service.ParentChildResolver;
import com.lassis.springframework.crud.service.SimpleCrudService;
import com.lassis.springframework.crud.service.UpdateValuesSetter;
import com.lassis.springframework.crud.util.EndpointsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.ResolvableType.forClassWithGenerics;

@Slf4j
@RequiredArgsConstructor
class CRUDServiceImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry bdr) {
        CRUDProperties config = EndpointsUtil.getConfig();

        bdr.registerBeanDefinition("endpointsConfig",
                rootBeanDefinition(CRUDProperties.class, () -> config).getBeanDefinition()
        );

        config.getEndpoints().forEach(e -> registryCrudService(bdr, e, ""));
    }


    private void registryCrudService(BeanDefinitionRegistry bdr, CRUDPathProperties endpoint, String prefixName) {
        final String beanName = createCrudServiceBeanName(endpoint, prefixName);

        if (!bdr.containsBeanDefinition(beanName)) {

            final BeanFactory bf = (BeanFactory) bdr;
            final Class<? extends WithId<? extends Serializable>> clazz = endpoint.getEntityClass();
            final Class<? extends Serializable> idClass = endpoint.getIdClass();

            final ResolvableType crudServiceType = forClassWithGenerics(CrudService.class, clazz, idClass);

            bdr.registerBeanDefinition(beanName,
                    rootBeanDefinition(crudServiceType, () -> {
                        final ResolvableType repositoryType = forClassWithGenerics(PagingAndSortingRepository.class, clazz, idClass);
                        final ResolvableType beforeSaveType = forClassWithGenerics(BeforeSave.class, clazz);
                        final ResolvableType updateSetterType = forClassWithGenerics(UpdateValuesSetter.class, clazz);

                        log.debug("required types for crud {} are:\n\trepository {}",
                                crudServiceType, repositoryType);

                        ObjectProvider<PagingAndSortingRepository<WithId<Serializable>, Serializable>> repositoryProvider = bf.getBeanProvider(repositoryType);
                        ObjectProvider<BeforeSave<WithId<Serializable>>> beforeSaveProvider = bf.getBeanProvider(beforeSaveType);
                        ObjectProvider<UpdateValuesSetter<WithId<Serializable>>> updateSetterProvider = bf.getBeanProvider(updateSetterType);

                        PagingAndSortingRepository<WithId<Serializable>, Serializable> repository = repositoryProvider.getObject();
                        log.debug("{} of type {} found", repository, repositoryType);

                        BeforeSave<WithId<Serializable>> beforeSave = beforeSaveProvider.getIfAvailable(BeforeSave::none);
                        log.debug("{} of type {} found", beforeSave, beforeSaveType);

                        UpdateValuesSetter<WithId<Serializable>> updateSetter = updateSetterProvider.getIfAvailable(GenericUpdateValuesSetter::new);
                        log.debug("{} of type {} found", updateSetter, updateSetterType);

                        CrudService<WithId<Serializable>, Serializable> rootService = new SimpleCrudService<>(
                                repository,
                                beforeSave,
                                updateSetter
                        );

                        log.info("bean {} of type {} has been created and it is now available in the context, " +
                                "if you like to override this bean create a bean with name {}", beanName, crudServiceType, beanName);

                        return createExecutorChain(endpoint, bf, rootService);
                    }).getBeanDefinition()
            );
        }

        endpoint.getEndpoints().forEach(sub -> registryCrudService(bdr, sub, prefixName + endpoint.getPath()));
    }

    private static CrudService<WithId<Serializable>, Serializable> createExecutorChain(CRUDPathProperties endpoint, BeanFactory bf, CrudService<WithId<Serializable>, Serializable> rootService) {
        final CRUDPathProperties parent = endpoint.getParent();
        if (Objects.isNull(parent)) {
            return rootService;
        }

        final Class<? extends WithId<? extends Serializable>> parentClazz = parent.getEntityClass();
        final Class<? extends WithId<? extends Serializable>> clazz = endpoint.getEntityClass();
        final Class<? extends Serializable> idClass = endpoint.getIdClass();

        ResolvableType subRepoType = forClassWithGenerics(ParentChildResolver.class, parentClazz, clazz, idClass);
        ObjectProvider<ParentChildResolver<WithId<Serializable>, WithId<Serializable>, Serializable>> subRepoProvider = bf.getBeanProvider(subRepoType);
        ParentChildResolver<WithId<Serializable>, WithId<Serializable>, Serializable> subRepo = subRepoProvider.getObject();

        CrudService<WithId<Serializable>, Serializable> multiLevelService = new MultiLevelCrudService<>(rootService, subRepo);
        return createExecutorChain(endpoint.getParent(), bf, multiLevelService);
    }

    private String createCrudServiceBeanName(CRUDPathProperties endpoint, String prefixName) {
        return Stream.of(prefixName, endpoint.getPath(), "service")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceAll("\\W", ""))
                .collect(Collectors.joining("_", "crud_", ""));
    }

}
