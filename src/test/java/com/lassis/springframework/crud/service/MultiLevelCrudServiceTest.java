package com.lassis.springframework.crud.service;

import com.github.javafaker.Faker;
import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.exception.RelationshipNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;
import java.util.Optional;

import static com.lassis.springframework.crud.CrudEndpointsTest.newProduct;
import static com.lassis.springframework.crud.CrudEndpointsTest.newProductDetail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiLevelCrudServiceTest {
    private static final Faker FAKER = Faker.instance();
    @Mock
    CrudService<ProductDetail, Long> rootService;

    @Mock
    ParentChildResolver<Product, ProductDetail, Long> parentChildResolver;

    MultiLevelCrudService<Product, ProductDetail, Long> service;

    @BeforeEach
    void setup() {
        service = new MultiLevelCrudService<>(rootService, parentChildResolver);
    }

    @Test
    void should_create() {
        // given
        long productId = FAKER.number().randomNumber();
        Product product = new Product();
        product.setId(productId);

        when(parentChildResolver.findParentById(eq(productId)))
                .thenReturn(Optional.of(product));

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(FAKER.number().randomNumber());
        productDetail.setProduct(product);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(productId);

        // when
        service.create(chain, productDetail);

        // assert
        assertThat(chain).isEmpty();
        verify(rootService).create(any(), eq(productDetail));
        verify(parentChildResolver).setParent(eq(product), eq(productDetail));
    }

    @Test
    void should_update() {
        // given
        Product product = new Product();
        product.setId(FAKER.number().randomNumber());

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(FAKER.number().randomNumber());
        productDetail.setProduct(product);

        when(parentChildResolver.existsByParentIdAndId(eq(product.getId()), eq(productDetail.getId())))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());

        // when
        service.update(chain, productDetail.getId(), productDetail);

        // assert
        assertThat(chain).isEmpty();
        verify(rootService).update(any(), eq(productDetail.getId()), eq(productDetail));
    }

    @Test
    void should_get() {
        // given
        Product product = new Product();
        product.setId(FAKER.number().randomNumber());

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(FAKER.number().randomNumber());

        when(parentChildResolver.existsByParentIdAndId(eq(product.getId()), eq(productDetail.getId())))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());

        // then
        service.get(chain, productDetail.getId());

        // assert
        assertThat(chain).isEmpty();
        verify(rootService).get(any(), eq(productDetail.getId()));
    }

    @Test
    void should_find_all() {
        // given
        Product product = new Product();
        product.setId(FAKER.number().randomNumber());

        when(parentChildResolver.existsByParentId(eq(product.getId())))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());

        Pageable unpaged = Pageable.unpaged();

        // then
        service.all(chain, unpaged);

        // when
        assertThat(chain).isEmpty();
        verify(parentChildResolver).findAllByParentId(eq(product.getId()), eq(unpaged));
    }

    @Test
    void should_delegate_find_all() {
        // given
        Product product = new Product();
        product.setId(FAKER.number().randomNumber());

        ProductDetail detail = newProductDetail(product);
        detail.setId(FAKER.number().randomNumber());

        when(parentChildResolver.existsByParentIdAndId(eq(product.getId()), eq(detail.getId())))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());
        chain.add(detail.getId());

        Pageable unpaged = Pageable.unpaged();

        // then
        service.all(chain, unpaged);

        // when
        assertThat(chain).hasSize(1);
        verify(rootService).all(any(), eq(unpaged));
    }

    @Test
    void should_delete() {
        // given
        Product product = new Product();
        product.setId(FAKER.number().randomNumber());

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(FAKER.number().randomNumber());

        when(parentChildResolver.existsByParentIdAndId(eq(product.getId()), eq(productDetail.getId())))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());

        // when
        service.deleteById(chain, productDetail.getId());

        // assert
        assertThat(chain).isEmpty();
        verify(rootService).deleteById(any(), eq(productDetail.getId()));
    }

    @Test
    void should_not_find_parent() {
        // given
        Product product = newProduct();
        product.setId(FAKER.number().randomNumber());

        ProductDetail productDetail = newProductDetail(null);
        productDetail.setId(FAKER.number().randomNumber());

        LinkedList<Long> chain = new LinkedList<>();

        when(parentChildResolver.findParentById(eq(product.getId())))
                .thenReturn(Optional.empty());
        when(parentChildResolver.existsByParentIdAndId(eq(product.getId()), eq(productDetail.getId())))
                .thenReturn(false);
        when(parentChildResolver.existsByParentId(eq(product.getId())))
                .thenReturn(false);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.create(chain, productDetail)).isInstanceOf(NotFoundException.class);
        assertThat(chain).isEmpty();

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.update(chain, productDetail.getId(), productDetail)).isInstanceOf(RelationshipNotFoundException.class);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.get(chain, productDetail.getId())).isInstanceOf(RelationshipNotFoundException.class);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.all(chain, Pageable.unpaged())).isInstanceOf(NotFoundException.class);
        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.deleteById(chain, productDetail.getId())).isInstanceOf(RelationshipNotFoundException.class);

    }
}
