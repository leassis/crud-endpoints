package com.lassis.springframework.crud.util;

import com.lassis.springframework.crud.configuration.CRUDPathProperties;
import com.lassis.springframework.crud.configuration.CRUDProperties;
import com.lassis.springframework.crud.fake.Clz1;
import com.lassis.springframework.crud.fake.Clz2;
import com.lassis.springframework.crud.fake.Clz2_1;
import com.lassis.springframework.crud.fake.Clz2_2;
import com.lassis.springframework.crud.fake.Clz3;
import com.lassis.springframework.crud.fake.Clz5;
import com.lassis.springframework.crud.fake.Clz6;
import com.lassis.springframework.crud.pojo.DtoType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

class EndpointsUtilTest {

    @Test
    void should_load() {
        CRUDProperties config = EndpointsUtil.getConfig(new ClassPathResource("fake-endpoints.yaml"));
        assertThat(config.getBasePath()).isEqualTo("/api");
        Set<CRUDPathProperties> endpoints = config.getEndpoints();
        assertThat(endpoints).hasSize(1);

        CRUDPathProperties endpoint = endpoints.iterator().next();
        assertThat(endpoint.getPath()).isEqualTo("/products");
        assertThat(endpoint.getEntityClass()).isEqualTo(Clz1.class);
        assertThat(endpoint.getPageSize()).isEqualTo(25);
        assertThat(endpoint.getMethods()).containsExactlyInAnyOrder(DELETE, GET, PUT, POST);
        assertThat(endpoint.getDtoClass(DtoType.GET)).isEqualTo(Clz2_1.class);
        assertThat(endpoint.getDtoClass(DtoType.POST)).isEqualTo(Clz2_2.class);
        assertThat(endpoint.getDtoClass(DtoType.PUT)).isEqualTo(Clz2.class);
        assertThat(endpoint.getDtoClass(DtoType.LIST)).isEqualTo(Clz2.class);

        ArrayList<CRUDPathProperties> subPaths = new ArrayList<>(endpoint.getEndpoints());
        subPaths.sort(Comparator.comparing(CRUDPathProperties::getPath));
        assertThat(subPaths).hasSize(2);

        final Iterator<CRUDPathProperties> itSub = subPaths.iterator();

        CRUDPathProperties sub = itSub.next();
        assertThat(sub.getPath()).isEqualTo("/asub");
        assertThat(sub.getEntityClass()).isEqualTo(Clz5.class);
        assertThat(sub.getPageSize()).isEqualTo(5);
        assertThat(sub.getMethods()).containsExactlyInAnyOrder(GET);

        Set<CRUDPathProperties> subSubPaths = sub.getEndpoints();
        assertThat(subSubPaths).hasSize(1);
        CRUDPathProperties subSub = subSubPaths.iterator().next();
        assertThat(subSub.getPath()).isEqualTo("/asub-sub");
        assertThat(subSub.getEntityClass()).isEqualTo(Clz6.class);
        assertThat(subSub.getDtoClass(DtoType.POST)).isEqualTo(Clz6.class);
        assertThat(subSub.getPageSize()).isEqualTo(5);
        assertThat(subSub.getMethods()).containsExactlyInAnyOrder(POST);

        // DETAILS
        sub = itSub.next();
        assertThat(sub.getPath()).isEqualTo("/details");
        assertThat(sub.getEntityClass()).isEqualTo(Clz3.class);
        assertThat(sub.getPageSize()).isEqualTo(10);
        assertThat(sub.getMethods()).containsExactlyInAnyOrder(GET);
    }

    @Test
    void should_test_defaults(){
        CRUDProperties config = EndpointsUtil.getConfig(new ClassPathResource("fake-2-endpoints.yaml"));
        assertThat(config.getBasePath()).isEqualTo("/api");
        Set<CRUDPathProperties> endpoints = config.getEndpoints();
        assertThat(endpoints).hasSize(1);

        CRUDPathProperties endpoint = endpoints.iterator().next();
        assertThat(endpoint.getPath()).isEqualTo("/products");
        assertThat(endpoint.getEntityClass()).isEqualTo(Clz1.class);
        assertThat(endpoint.getPageSize()).isEqualTo(25);
        assertThat(endpoint.getMethods()).containsExactlyInAnyOrder(DELETE, GET, POST);
        assertThat(endpoint.getDtoClass(DtoType.GET)).isEqualTo(Clz1.class);
        assertThat(endpoint.getDtoClass(DtoType.POST)).isEqualTo(Clz1.class);
        assertThat(endpoint.getDtoClass(DtoType.PUT)).isEqualTo(Clz1.class);
        assertThat(endpoint.getDtoClass(DtoType.LIST)).isEqualTo(Clz1.class);

        final Iterator<CRUDPathProperties> itSub = endpoint.getEndpoints().iterator();
        CRUDPathProperties sub = itSub.next();
        assertThat(sub.getPath()).isEqualTo("/details");
        assertThat(sub.getEntityClass()).isEqualTo(Clz2.class);
        assertThat(sub.getPageSize()).isEqualTo(25);
        assertThat(sub.getDtoClass(DtoType.GET)).isEqualTo(Clz2.class);
        assertThat(sub.getDtoClass(DtoType.POST)).isEqualTo(Clz2.class);
        assertThat(sub.getDtoClass(DtoType.PUT)).isEqualTo(Clz2.class);
        assertThat(sub.getDtoClass(DtoType.LIST)).isEqualTo(Clz2.class);
    }

}
