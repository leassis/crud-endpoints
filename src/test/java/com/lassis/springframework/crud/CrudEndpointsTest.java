package com.lassis.springframework.crud;

import com.github.javafaker.Faker;
import com.lassis.springframework.crud.configuration.EnableCrud;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableCrud
@EnableJpaRepositories
@EnableWebMvc
@EntityScan
@ComponentScan
@WebAppConfiguration
@AutoConfigureTestEntityManager
@AutoConfigureDataJpa
@AutoConfigureMockMvc
class CrudEndpointsTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired(required = false)
    CrudService<Product, Long> crudService;

    @Autowired
    ProductRepository productRepository;

    @Test
    void shouldDoAGetRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void shouldCreateCrudService() {
        assertThat(crudService).isNotNull();
    }

    @Test
    void shouldProductRepository() {
        assertThat(productRepository).isNotNull();

        String name = Faker.instance().name().firstName();
        Product product = new Product();
        product.setName(name);
        productRepository.save(product);

        assertThat(productRepository.findById(1L)).isPresent();
    }
}
