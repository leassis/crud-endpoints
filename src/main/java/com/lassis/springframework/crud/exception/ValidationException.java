package com.lassis.springframework.crud.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class ValidationException extends Exception {
    private final transient Set<ConstraintViolation<Serializable>> errors;
}
