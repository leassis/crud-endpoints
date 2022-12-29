package com.lassis.springframework.crud.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor
@Value
public class BodyValidation {
    Set<BodyContent> violations;

    @Value
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BodyContent {
        String field;
        String reason;
    }
}
