package com.lassis.springframework.crud.api;

import com.lassis.springframework.crud.pojo.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.function.ServerRequest;

import java.io.Serializable;

public interface PaginationConverter {

    Pagination toPagination(Page<Serializable> pageContent);

    Pageable getPageable(ServerRequest req, int size);
}
