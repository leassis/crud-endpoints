package com.lassis.springframework.crud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(Serializable id) {
        super(id + " not found");
    }

    public NotFoundException() {
        super("element not found");
    }
}
