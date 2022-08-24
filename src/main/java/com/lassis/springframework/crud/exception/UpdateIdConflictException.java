package com.lassis.springframework.crud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UpdateIdConflictException extends RuntimeException {
    public UpdateIdConflictException(Serializable pathId, Serializable bodyId) {
        super("body id <" + bodyId + "> and path id <" + pathId + "> are different");
    }
}
