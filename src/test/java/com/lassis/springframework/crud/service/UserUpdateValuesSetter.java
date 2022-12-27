package com.lassis.springframework.crud.service;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class UserUpdateValuesSetter implements UpdateValuesSetter<User> {

    @Override
    public void update(User entity, User in) {
        if (isNull(entity.getId()) && nonNull(in.getId())) {
            entity.setId(in.getId());
        }

        entity.setName(in.getName());
    }
}
