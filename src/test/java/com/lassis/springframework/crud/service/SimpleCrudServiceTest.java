package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.exception.NotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.LinkedList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleCrudServiceTest {

    @Mock
    PagingAndSortingRepository<Product, Long> repository;

    @Mock
    BeforeSave<Product> beforeSave;

    @Mock
    UpdateValuesSetter<Product> updateSetter;


    CrudService<Product, Long> service;

    @BeforeEach
    void setup() {
        service = new SimpleCrudService<>(repository, beforeSave, updateSetter);
    }

    @Test
    void shouldUGetProduct() {
        Product p = Instancio.create(Product.class);
        Long id = p.getId();

        when(repository.findById(id)).thenReturn(Optional.of(p));

        p = service.get(new LinkedList<>(), id);
        assertThat(p).isNotNull();
    }

    @Test
    void shouldNotGetProductProductNotFound() {
        long id = Instancio.create(Long.class);

        when(repository.findById(id)).thenReturn(Optional.empty());
        final LinkedList<Long> chain = new LinkedList<>();
        NotFoundException ex = catchThrowableOfType(() -> service.get(chain, id), NotFoundException.class);
        assertThat(ex.getMessage()).contains(id + " not found");
    }


    @Test
    void shouldCreateProduct() {
        Product p = Instancio.create(Product.class);
        p.setId(null);

        doAnswer(inv -> {
            Product prd0 = inv.getArgument(0);
            Product prd1 = inv.getArgument(1);
            prd0.setName(prd1.getName());
            return null;
        }).when(updateSetter).update(eq(p), any());

        when(repository.save(any())).then((Answer<Product>) invocation -> {
            Product prd = invocation.getArgument(0);
            prd.setId(1L);
            return prd;
        });
        when(repository.findById(any())).thenReturn(Optional.of(p));

        service.create(new LinkedList<>(), p);

        verify(beforeSave).execute(any());
        verify(repository).save(any());
    }

    @Test
    void shouldUpdateProduct() {
        Product p = Instancio.create(Product.class);
        Long id = p.getId();

        when(repository.findById(id)).thenReturn(Optional.of(p));
        when(repository.save(p)).thenReturn(p);
        service.update(new LinkedList<>(), id, p);

        verify(updateSetter).update(p, p);
        verify(beforeSave).execute(p);
        verify(repository).save(p);
    }

    @Test
    void shouldNotUpdateProductNotFound() {
        long id = Instancio.create(Long.class);

        when(repository.findById(id)).thenReturn(Optional.empty());

        Product p = new Product();
        p.setId(id);

        LinkedList<Long> chain = new LinkedList<>();
        NotFoundException ex = catchThrowableOfType(() -> service.update(chain, id, p), NotFoundException.class);

        assertThat(ex.getMessage()).contains(id + " not found");
        verify(repository, times(0)).save(p);
    }

    @Test
    void shouldDeleteProduct() {
        long id = Instancio.create(Long.class);

        when(repository.existsById(id)).thenReturn(true);

        service.deleteById(new LinkedList<>(), id);

        verify(repository).deleteById(id);
    }

    @Test
    void shouldNotDeleteProductProductNotFound() {
        long id = Instancio.create(Long.class);
        when(repository.existsById(id)).thenReturn(false);

        LinkedList<Long> chain = new LinkedList<>();
        NotFoundException ex = catchThrowableOfType(() -> service.deleteById(chain, id), NotFoundException.class);

        assertThat(ex.getMessage()).contains(id + " not found");
    }

    @Test
    void shouldGetAllProducts() {
        service.all(new LinkedList<>(), Pageable.unpaged());
        verify(repository).findAll(any(Pageable.class));
    }
}
