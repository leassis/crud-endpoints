package com.lassis.springframework.crud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RelationshipNotFoundException extends RuntimeException {

    public RelationshipNotFoundException() {
        super("relationship not found");
    }
}
