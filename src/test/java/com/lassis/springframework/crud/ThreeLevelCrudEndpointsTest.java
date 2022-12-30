package com.lassis.springframework.crud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lassis.springframework.crud.configuration.EnableCrud;
import com.lassis.springframework.crud.matcher.CaptorMatcher;
import com.lassis.springframework.crud.repository.ProductDetailLanguageRepository;
import com.lassis.springframework.crud.repository.ProductDetailRepository;
import com.lassis.springframework.crud.repository.ProductRepository;
import com.lassis.springframework.crud.service.Language;
import com.lassis.springframework.crud.service.Product;
import com.lassis.springframework.crud.service.ProductDetail;
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

import static com.lassis.springframework.crud.CrudEndpointsTest.getProductDetailLanguage;
import static com.lassis.springframework.crud.CrudEndpointsTest.newProduct;
import static com.lassis.springframework.crud.CrudEndpointsTest.newProductDetail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
class ThreeLevelCrudEndpointsTest {

    @Autowired
    MockMvc mockMvc;

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
    void should_create() throws Exception {
        Product p = newProduct();

        final String urlTemplate = "/api/products";

        CaptorMatcher<Long> idCaptorMatcher = new CaptorMatcher<>(notNullValue(Long.class));
        mockMvc.perform(post(p, urlTemplate))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", idCaptorMatcher, Long.class));

        Long productId = idCaptorMatcher.getLastValue().orElseThrow(() -> new AssertionError("product id response is null"));

        ProductDetail d = newProductDetail(p);
        mockMvc.perform(post(d, urlTemplate + "/" + productId + "/details"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", idCaptorMatcher, Long.class));
        Long detailId = idCaptorMatcher.getLastValue().orElseThrow(() -> new AssertionError("detail id response is null"));

        Language dmax = getProductDetailLanguage(d);
        mockMvc.perform(post(dmax, urlTemplate + "/" + productId + "/details/" + detailId + "/languages"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", idCaptorMatcher, Long.class));
    }

    @Test
    void should_do_all_but_update() throws Exception {
        Product p = newProduct();
        p = productRepository.save(p);

        ProductDetail d = newProductDetail(p);
        d = productDetailRepository.save(d);

        Language lang = getProductDetailLanguage(d);
        lang = productDetailLanguageRepository.save(lang);

        String url = "/api/products/" + p.getId() + "/details/" + d.getId() + "/languages";

        // get all
        MockHttpServletRequestBuilder getAll = MockMvcRequestBuilders.get(url)
                .accept(MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(getAll)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(lang.getId()), Long.class));

        // get
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url + "/" + lang.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(lang.getId()), Long.class));

        // put
        MockHttpServletRequestBuilder put = MockMvcRequestBuilders.put(url + "/" + lang.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(lang));

        mockMvc.perform(put)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(lang.getId()), Long.class));

        // delete
        MockHttpServletRequestBuilder delete = MockMvcRequestBuilders.delete(url + "/" + lang.getId())
                .accept(MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(delete)
                .andExpect(status().isNoContent());
    }

    private MockHttpServletRequestBuilder post(Object obj, String urlTemplate) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(urlTemplate)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(obj));
    }

}
