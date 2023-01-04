package com.lassis.springframework.crud.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericUpdateValuesSetterTest {

    private final GenericUpdateValuesSetter<Product> setter = new GenericUpdateValuesSetter<>();

    @Test
    void copy_all_values(){
        Product nieuwe = Instancio.create(Product.class);

        long oldId = Instancio.create(Long.class);
        Product old = Instancio.create(Product.class);
        old.setId(oldId);

        setter.update(old, nieuwe);
        assertThat(old.getId()).isEqualTo(oldId);
        assertThat(old.getName()).isEqualTo(nieuwe.getName());
        assertThat(old.getDescription()).isEqualTo(nieuwe.getDescription());
    }


}
