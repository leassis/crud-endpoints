package com.lassis.springframework.crud.configuration;

import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.repository.CRUDRepository;
import com.lassis.springframework.crud.service.BeforeSave;
import com.lassis.springframework.crud.service.CrudService;
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

import java.io.Serializable;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.ResolvableType.forClassWithGenerics;

@Slf4j
@RequiredArgsConstructor
class CRUDServiceImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry bdr) {
        CRUDEndpointsConfig config = EndpointsUtil.getConfig();

        bdr.registerBeanDefinition("endpointsConfig",
                rootBeanDefinition(CRUDEndpointsConfig.class, () -> config).getBeanDefinition()
        );

        for (CRUDEndpointConfig endpoint : config.getEndpoints()) {
            registryCrudService(bdr, endpoint);
        }
    }


    private void registryCrudService(BeanDefinitionRegistry bdr, CRUDEndpointConfig endpoint) {
        Class<? extends WithId<? extends Serializable>> clazz = endpoint.getEntityClass();
        Class<? extends Serializable> idClass = endpoint.getIdClass();

        ResolvableType crudServiceType = forClassWithGenerics(CrudService.class, clazz, idClass);
        ResolvableType repositoryType = forClassWithGenerics(CRUDRepository.class, clazz, idClass);
        ResolvableType beforeSaveType = forClassWithGenerics(BeforeSave.class, clazz);
        ResolvableType updateSetterType = forClassWithGenerics(UpdateValuesSetter.class, clazz);

        String beanName = createCrudServiceBeanName(endpoint);
        bdr.registerBeanDefinition(beanName,
                rootBeanDefinition(crudServiceType, () -> {
                    log.debug("required types for crud {} are:\n\trepository {}\n\tbeforeSave: {}\n\tbeforeUpdate: {}",
                            crudServiceType, repositoryType, beforeSaveType, updateSetterType);

                    BeanFactory bf = (BeanFactory) bdr;

                    ObjectProvider<CRUDRepository<WithId<Serializable>, Serializable>> repositoryProvider = bf.getBeanProvider(repositoryType);
                    ObjectProvider<BeforeSave<WithId<Serializable>>> beforeSaveProvider = bf.getBeanProvider(beforeSaveType);
                    ObjectProvider<UpdateValuesSetter<WithId<Serializable>>> updateSetterProvider = bf.getBeanProvider(updateSetterType);

                    CRUDRepository<WithId<Serializable>, Serializable> repository = repositoryProvider.getObject();
                    log.debug("{} of type {} found", repository, repositoryType);

                    BeforeSave<WithId<Serializable>> beforeSave = beforeSaveProvider.getIfAvailable(BeforeSave::none);
                    log.debug("{} of type {} found", beforeSave, beforeSaveType);

                    UpdateValuesSetter<WithId<Serializable>> updateSetter = updateSetterProvider.getObject();
                    log.debug("{} of type {} found", updateSetter, updateSetterType);

                    CrudService<WithId<Serializable>, Serializable> result = new CrudService<>(
                            repository,
                            beforeSave,
                            updateSetter
                    );
                    log.info("bean {} of type {} has been created and it is now available in the context", beanName, crudServiceType);

                    return result;
                }).getBeanDefinition()
        );
    }

    private String createCrudServiceBeanName(CRUDEndpointConfig endpoint) {
        return endpoint.getPath().replaceAll("\\W", "_") + "_service";
    }

}
