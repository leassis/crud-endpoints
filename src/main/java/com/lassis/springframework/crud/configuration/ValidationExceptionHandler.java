package com.lassis.springframework.crud.configuration;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ValidationException;

@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler({ValidationException.class})
    void handler(){
        System.out.println("######### ValidationException");
    }
}
