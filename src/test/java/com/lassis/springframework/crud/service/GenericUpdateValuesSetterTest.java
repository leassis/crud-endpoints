package com.lassis.springframework.crud.service;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericUpdateValuesSetterTest {
    private static final Faker FAKER = Faker.instance();

    private final GenericUpdateValuesSetter<Product> setter = new GenericUpdateValuesSetter<>();

    @Test
    void copy_all_values(){
        final String newName = FAKER.funnyName().name();
        final String newDescription = FAKER.backToTheFuture().quote();
        Product nieuwe = new Product();
        nieuwe.setId(FAKER.number().randomNumber());
        nieuwe.setName(newName);
        nieuwe.setDescription(newDescription);

        long oldId = FAKER.number().randomNumber();
        Product old = new Product();
        old.setId(oldId);
        old.setName(FAKER.funnyName().name());
        old.setDescription(FAKER.shakespeare().hamletQuote());

        setter.update(old, nieuwe);
        assertThat(old.getId()).isEqualTo(oldId);
        assertThat(old.getName()).isEqualTo(newName);
        assertThat(old.getDescription()).isEqualTo(newDescription);
    }


}
