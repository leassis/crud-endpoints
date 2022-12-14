package com.lassis.springframework.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lassis.springframework.crud.configuration.EnableCrud;
import com.lassis.springframework.crud.entity.WithId;
import com.lassis.springframework.crud.repository.ProductDetailLanguageRepository;
import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.CrudService;
import com.lassis.springframework.crud.service.Language;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
import com.lassis.springframework.crud.service.User;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
public class CrudEndpointsTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired(required = false)
    CrudService<Product, Long> crudService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Autowired
    ProductDetailLanguageRepository productDetailLanguageRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        productDetailLanguageRepository.deleteAll();
        productDetailRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldTryDoCreateWithProductId() throws Exception {
        Product p = newProduct();
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
        Product p = newProduct();

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/products")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(p));

        mockMvc.perform(post)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDoGet() throws Exception {
        Product p = newProduct();
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
        Product p = newProduct();
        p = productRepository.save(p);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId()).accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(jsonPath("$.data.name", is(p.getName())))
                .andExpect(jsonPath("$.data.id", is(p.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDoUpdate() throws Exception {
        Product p = newProduct();
        p = productRepository.save(p);

        p.setName(Instancio.create(String.class));

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
        Product create = newProduct();
        create = productRepository.save(create);

        Product update = newProduct();
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
        List<Product> toSave = Instancio.of(baseModel(Product.class))
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<Product> products = StreamSupport.stream(productRepository.saveAll(toSave).spliterator(), false)
                .sorted(Comparator.comparing(Product::getId))
                .collect(Collectors.toList());


        final int pageSize = 3;
        // page 0
        int elem = 0;
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products?size=" + pageSize).accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andDo(print())
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
                .andDo(print())
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
                .andDo(print())
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
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data", hasSize(pageSize)))
                .andExpect(jsonPath("$.data[0].id", is(products.get(elem).getId()), Long.class))
                .andExpect(jsonPath("$.data[0].name", is(products.get(elem).getName())))
                .andExpect(jsonPath("$.meta.first", is("F0S3")))
                .andExpect(jsonPath("$.meta.prev", is("F0S3")))
                .andExpect(jsonPath("$.meta.next", is("P2S3")));
    }

    @Test
    void shouldCreateCrudService() {
        assertThat(crudService).isNotNull();
    }


    @Test
    void shouldDoATwoLevelGetAll() throws Exception {
        Product p = newProduct();
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(p);
        d = productDetailRepository.save(d);

        Product p1 = newProduct();
        productRepository.save(p1);

        ProductDetail d1 = newProductDetail(p1);
        productDetailRepository.save(d1);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId() + "/details")
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].detail", is(d.getDetail())))
                .andExpect(jsonPath("$.data[0].id", is(d.getId()), Long.class));
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
        Product p = newProduct();
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(p);
        d = productDetailRepository.save(d);

        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/api/products/" + p.getId() + "/details/" + d.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE);
        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.detail", is(d.getDetail())))
                .andExpect(jsonPath("$.data.id", is(d.getId()), Long.class));
    }

    @Test
    void shouldDoATwoLevelCreate() throws Exception {
        Product p = newProduct();
        p = productRepository.save(p);

        ProductDetail detail = newProductDetail(p);

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/products/" + p.getId() + "/details")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(detail));

        mockMvc.perform(post)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.detail", is(detail.getDetail())))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    void shouldDoCreateAUserAndReturnDto() throws Exception {
        User u = newUser();

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(u));

        mockMvc.perform(post)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(u.getName()))
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void shouldFailOnValidation() throws Exception {
        User u = new User();

        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/api/users")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(u));

        mockMvc.perform(post)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field").value("name"))
                .andExpect(jsonPath("$.violations[0].reason").value("must not be blank"));
    }

    public static Product newProduct() {
        return Instancio.create(baseModel(Product.class));
    }

    public static ProductDetail newProductDetail(Product product) {
        return Instancio.of(baseModel(ProductDetail.class))
                .set(Select.all(Product.class), product)
                .create();
    }

    public static User newUser() {
        return Instancio.create(baseModel(User.class));
    }

    public static Language getProductDetailLanguage(ProductDetail detail) {
        return Instancio.of(baseModel(Language.class))
                .set(Select.all(ProductDetail.class), detail)
                .create();
    }

    public static <T extends WithId<? extends Serializable>> Model<T> baseModel(Class<T> clazz) {
        return Instancio.of(clazz)
                .ignore(Select.field("id"))
                .toModel();
    }

}
