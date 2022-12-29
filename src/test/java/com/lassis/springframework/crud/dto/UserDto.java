package com.lassis.springframework.crud.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Valid
public class UserDto implements Serializable {
    @NotBlank
    private String name;
}
