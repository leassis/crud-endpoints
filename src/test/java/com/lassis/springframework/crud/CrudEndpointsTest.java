package com.lassis.springframework.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.lassis.springframework.crud.configuration.EnableCrud;
import com.lassis.springframework.crud.repository.ProductDetailMaxRepository;
import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
import com.lassis.springframework.crud.service.ProductDetailMax;
import com.lassis.springframework.crud.service.User;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final Faker FAKER = Faker.instance();

    @Autowired
    MockMvc mockMvc;

    @Autowired(required = false)
    CrudService<Product, Long> crudService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Autowired
    ProductDetailMaxRepository productDetailMaxRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        productDetailMaxRepository.deleteAll();
        productDetailRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldTryDoCreateWithProductId() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p.setId(10L);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/products")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(p));

        mockMvc.perform(post)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDoCreate() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/products")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(p));

        mockMvc.perform(post)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoGet() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products").accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is(p.getName())))
                .andExpect(jsonPath("$.data[0].id", is(p.getId()), Long.class))
                .andExpect(jsonPath("$.meta.first", is("F0S25")))
                .andExpect(jsonPath("$.meta.prev", nullValue()))
                .andExpect(jsonPath("$.meta.next", nullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoAGetNotFoundById() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/99999").accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDoAGetById() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId()).accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", is(p.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoUpdate() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        p.setName(FAKER.backToTheFuture().character());

        MockHttpServletRequestBuilder put = MockMvcRequestBuilders.put("/api/products/" + p.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(p));

        mockMvc.perform(put)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", is(p.getId()), Long.class))
                .andExpect(status().isOk());

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId()).accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", is(p.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldTryDoUpdateWithConflictId() throws Exception {
        Product create = newProduct(FAKER.funnyName().name());
        create = productRepository.save(create);

        Product update = newProduct(FAKER.funnyName().name());
        update.setId(10L);

        MockHttpServletRequestBuilder put = MockMvcRequestBuilders.put("/api/products/" + create.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(update));

        mockMvc.perform(put)
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldDoAGetWithPagination() throws Exception {
        List<Product> toSave = IntStream.range(0, 10)
                .mapToObj(i -> newProduct(FAKER.funnyName().name()))
                .collect(Collectors.toList());

        List<Product> products = StreamSupport.stream(productRepository.saveAll(toSave).spliterator(), false)
                .sorted(Comparator.comparing(Product::getId))
                .collect(Collectors.toList());


        final int pageSize = 3;
        // page 0
        int elem = 0;
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products?size=" + pageSize).accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.data", hasSize(pageSize)))
                .andExpect(jsonPath("$.data[0].id", is(products.get(elem).getId()), Long.class))
                .andExpect(jsonPath("$.data[0].name", is(products.get(elem).getName())))
                .andExpect(jsonPath("$.meta.first", is("F0S3")))
                .andExpect(jsonPath("$.meta.prev", nullValue()))
                .andExpect(jsonPath("$.meta.next", is("P1S3")))
                .andExpect(status().isOk());

        // page 1
        elem = 3;
        get = MockMvcRequestBuilders.get("/api/products?page=P1S3").accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.data", hasSize(pageSize)))
                .andExpect(jsonPath("$.data[0].id", is(products.get(elem).getId()), Long.class))
                .andExpect(jsonPath("$.data[0].name", is(products.get(elem).getName())))
                .andExpect(jsonPath("$.meta.first", is("F0S3")))
                .andExpect(jsonPath("$.meta.prev", is("F0S3")))
                .andExpect(jsonPath("$.meta.next", is("P2S3")))
                .andExpect(status().isOk());

        // page 2
        elem = 6;
        get = MockMvcRequestBuilders.get("/api/products?page=P2S3").accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.data", hasSize(pageSize)))
                .andExpect(jsonPath("$.data[0].id", is(products.get(elem).getId()), Long.class))
                .andExpect(jsonPath("$.data[0].name", is(products.get(elem).getName())))
                .andExpect(jsonPath("$.meta.first", is("F0S3")))
                .andExpect(jsonPath("$.meta.prev", is("P1S3")))
                .andExpect(jsonPath("$.meta.next", is("P3S3")))
                .andExpect(status().isOk());

        // page 1 again
        elem = 3;
        get = MockMvcRequestBuilders.get("/api/products?page=P1S3").accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.data", hasSize(pageSize)))
                .andExpect(jsonPath("$.data[0].id", is(products.get(elem).getId()), Long.class))
                .andExpect(jsonPath("$.data[0].name", is(products.get(elem).getName())))
                .andExpect(jsonPath("$.meta.first", is("F0S3")))
                .andExpect(jsonPath("$.meta.prev", is("F0S3")))
                .andExpect(jsonPath("$.meta.next", is("P2S3")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateCrudService() {
        assertThat(crudService).isNotNull();
    }

    private static Product newProduct(String defaultNewProductName) {
        Product p = new Product();
        p.setName(defaultNewProductName);
        return p;
    }


    @Test
    void shouldDoATwoLevelGetAll() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(FAKER.funnyName().name(), p);
        d = productDetailRepository.save(d);

        Product p1 = newProduct(FAKER.funnyName().name());
        productRepository.save(p1);

        ProductDetail d1 = newProductDetail(FAKER.funnyName().name(), p1);
        productDetailRepository.save(d1);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId() + "/details")
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].detail", is(d.getDetail())))
                .andExpect(jsonPath("$.data[0].id", is(d.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldBeNotFoundATwoLevelGet() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/9999/details")
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDoATwoLevelGetById() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(FAKER.funnyName().name(), p);
        d = productDetailRepository.save(d);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId() + "/details/" + d.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data.detail", is(d.getDetail())))
                .andExpect(jsonPath("$.data.id", is(d.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoATwoLevelCreate() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        ProductDetail detail = newProductDetail(FAKER.funnyName().name(), p);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/products/" + p.getId() + "/details")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(detail));

        mockMvc.perform(post)
                .andExpect(jsonPath("$.data.detail", is(detail.getDetail())))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoAThreeLevelGetAll() throws Exception {
        Product p = newProduct(FAKER.funnyName().name());
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(FAKER.funnyName().name(), p);
        d = productDetailRepository.save(d);

        ProductDetailMax dmax = new ProductDetailMax();
        dmax.setProductDetail(d);
        dmax = productDetailMaxRepository.save(dmax);

        Product p1 = newProduct(FAKER.funnyName().name());
        productRepository.save(p1);

        ProductDetail d1 = newProductDetail(FAKER.funnyName().name(), p1);
        productDetailRepository.save(d1);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId() + "/details/" + d.getId() + "/details-max")
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(dmax.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoCreateAUserAndReturnDto() throws Exception {
        User u = new User();
        u.setName(FAKER.funnyName().name());


        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(u));

        mockMvc.perform(post)
                .andExpect(jsonPath("$.data.name").value(u.getName()))
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(status().isOk());
    }

    private static ProductDetail newProductDetail(String detail, Product product) {
        ProductDetail d = new ProductDetail();
        d.setDetail(detail);
        d.setProduct(product);
        return d;
    }

}
