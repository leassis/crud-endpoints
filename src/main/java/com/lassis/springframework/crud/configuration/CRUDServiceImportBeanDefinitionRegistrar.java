package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.service.BeforeSave;
import com.lassis.springframework.crud.service.ChainChecker;
import com.lassis.springframework.crud.service.ChainCheckerNoOp;
import com.lassis.springframework.crud.service.CrudService;
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

        config.getEndpoints().forEach(e -> registryCrudServiceIfNotRegistered(bdr, e, ""));
    }


    private void registryCrudServiceIfNotRegistered(BeanDefinitionRegistry bdr, CRUDPathProperties endpoint, String prefixName) {
        final BeanFactory bf = (BeanFactory) bdr;
        final Class<? extends WithId<? extends Serializable>> clazz = endpoint.getEntityClass();
        final Class<? extends Serializable> idClass = endpoint.getIdClass();

        final ResolvableType crudServiceType = forClassWithGenerics(CrudService.class, clazz, idClass);

        final ResolvableType repositoryType = forClassWithGenerics(PagingAndSortingRepository.class, clazz, idClass);
        final ResolvableType beforeSaveType = forClassWithGenerics(BeforeSave.class, clazz);
        final ResolvableType updateSetterType = forClassWithGenerics(UpdateValuesSetter.class, clazz);
        final ResolvableType chainCheckerType = forClassWithGenerics(ChainChecker.class, clazz, idClass);

        final String beanName = createCrudServiceBeanName(endpoint, prefixName);
        bdr.registerBeanDefinition(beanName,
                rootBeanDefinition(crudServiceType, () -> {
                    log.debug("required types for crud {} are:\n\trepository {}\n\tbeforeUpdate: {}",
                            crudServiceType, repositoryType, updateSetterType);

                    ObjectProvider<PagingAndSortingRepository<WithId<Serializable>, Serializable>> repositoryProvider = bf.getBeanProvider(repositoryType);
                    ObjectProvider<BeforeSave<WithId<Serializable>>> beforeSaveProvider = bf.getBeanProvider(beforeSaveType);
                    ObjectProvider<UpdateValuesSetter<WithId<Serializable>>> updateSetterProvider = bf.getBeanProvider(updateSetterType);
                    ObjectProvider<ChainChecker<WithId<Serializable>, Serializable>> chainCheckerProvider = bf.getBeanProvider(chainCheckerType);

                    PagingAndSortingRepository<WithId<Serializable>, Serializable> repository = repositoryProvider.getObject();
                    log.debug("{} of type {} found", repository, repositoryType);

                    BeforeSave<WithId<Serializable>> beforeSave = beforeSaveProvider.getIfAvailable(BeforeSave::none);
                    log.debug("{} of type {} found", beforeSave, beforeSaveType);

                    UpdateValuesSetter<WithId<Serializable>> updateSetter = updateSetterProvider.getObject();
                    log.debug("{} of type {} found", updateSetter, updateSetterType);

                    ChainChecker<WithId<Serializable>, Serializable> chainChecker = chainCheckerProvider.getIfAvailable(() -> new ChainCheckerNoOp<>(chainCheckerType));
                    log.debug("{} of type {} found", chainChecker, chainCheckerType);

                    CrudService<WithId<Serializable>, Serializable> result = new SimpleCrudService<>(
                            repository,
                            beforeSave,
                            updateSetter,
                            chainChecker
                    );
                    log.info("bean {} of type {} has been created and it is now available in the context", beanName, crudServiceType);

                    return result;
                }).getBeanDefinition()
        );

        endpoint.getSubPaths().forEach(sub -> registryCrudServiceIfNotRegistered(bdr, sub, prefixName + endpoint.getPath()));
    }

    private String createCrudServiceBeanName(CRUDPathProperties endpoint, String prefixName) {
        return Stream.of(prefixName, endpoint.getPath(), "service")
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceAll("\\W", ""))
                .collect(Collectors.joining("_", "crud_", ""));
    }

}
