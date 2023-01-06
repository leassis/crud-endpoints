package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.exception.NotFoundException;
import com.lassis.springframework.crud.exception.RelationshipNotFoundException;
import org.instancio.Instancio;
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
    private static final Pageable UNPAGED = Pageable.unpaged();

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
        long productId = Instancio.create(Long.class);
        Product product = new Product();
        product.setId(productId);

        when(parentChildResolver.findParentById(productId))
                .thenReturn(Optional.of(product));

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(Instancio.create(Long.class));
        productDetail.setProduct(product);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(productId);

        // when
        service.create(chain, productDetail);

        // assert
        assertThat(chain).isEmpty();
        verify(rootService).create(any(), eq(productDetail));
        verify(parentChildResolver).setParent(product, productDetail);
    }

    @Test
    void should_update() {
        // given
        Product product = new Product();
        product.setId(Instancio.create(Long.class));

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(Instancio.create(Long.class));
        productDetail.setProduct(product);

        when(parentChildResolver.existsByParentIdAndId(product.getId(), productDetail.getId()))
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
        product.setId(Instancio.create(Long.class));

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(Instancio.create(Long.class));

        when(parentChildResolver.existsByParentIdAndId(product.getId(), productDetail.getId()))
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
        product.setId(Instancio.create(Long.class));

        when(parentChildResolver.existsByParentId(product.getId()))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());

        // then
        service.all(chain, UNPAGED);

        // when
        assertThat(chain).isEmpty();
        verify(parentChildResolver).findAllByParentId(product.getId(), UNPAGED);
    }

    @Test
    void should_delegate_find_all() {
        // given
        Product product = new Product();
        product.setId(Instancio.create(Long.class));

        ProductDetail detail = newProductDetail(product);
        detail.setId(Instancio.create(Long.class));

        when(parentChildResolver.existsByParentIdAndId(product.getId(), detail.getId()))
                .thenReturn(true);

        LinkedList<Long> chain = new LinkedList<>();
        chain.add(product.getId());
        chain.add(detail.getId());

        Pageable unpaged = UNPAGED;

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
        product.setId(Instancio.create(Long.class));

        ProductDetail productDetail = new ProductDetail();
        productDetail.setId(Instancio.create(Long.class));

        when(parentChildResolver.existsByParentIdAndId(product.getId(), productDetail.getId()))
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
        product.setId(Instancio.create(Long.class));

        ProductDetail productDetail = newProductDetail(null);
        productDetail.setId(Instancio.create(Long.class));

        LinkedList<Long> chain = new LinkedList<>();

        when(parentChildResolver.findParentById(product.getId()))
                .thenReturn(Optional.empty());
        when(parentChildResolver.existsByParentIdAndId(product.getId(), productDetail.getId()))
                .thenReturn(false);
        when(parentChildResolver.existsByParentId(product.getId()))
                .thenReturn(false);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.create(chain, productDetail)).isInstanceOf(NotFoundException.class);
        assertThat(chain).isEmpty();

        //
        Long productDetailId = productDetail.getId();
        chain.add(product.getId());
        assertThatThrownBy(() -> service.update(chain, productDetailId, productDetail)).isInstanceOf(RelationshipNotFoundException.class);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.get(chain, productDetailId)).isInstanceOf(RelationshipNotFoundException.class);

        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.all(chain, UNPAGED)).isInstanceOf(NotFoundException.class);
        //
        chain.add(product.getId());
        assertThatThrownBy(() -> service.deleteById(chain, productDetailId)).isInstanceOf(RelationshipNotFoundException.class);

    }
}
