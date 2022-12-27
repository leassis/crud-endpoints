package com.lassis.springframework.crud.dto;

import com.lassis.springframework.crud.service.DtoConverter;
import com.lassis.springframework.crud.service.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter implements DtoConverter<UserDto, User> {
    @Override
    public User fromDto(UserDto obj) {
        User entity = new User();
        entity.setName(obj.getName());
        return entity;
    }

    @Override
    public UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setName(entity.getName());
        return dto;
    }
}
