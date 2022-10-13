package com.lassis.springframework.crud.pojo;

import lombok.Value;

@Value(staticConstructor = "of")
public class Pagination {
    String first;
    String prev;
    String next;
}
