package com.lassis.springframework.crud.api;

import com.lassis.springframework.crud.pojo.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.function.ServerRequest;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public class SimplePaginationConverter implements PaginationConverter {
    private static final Pattern PAGE_PATTERN = Pattern.compile("^[PF]\\d+S\\d+$");

    @Override
    public Pagination toPagination(Page<Serializable> pageContent) {
        Pageable pageable = pageContent.getPageable();
        Pageable first = pageable.first();
        String tFirst = "F" + first.getPageNumber() + "S" + first.getPageSize();

        String tPrev = null;
        if (pageContent.hasPrevious()) {
            Pageable prev = pageable.previousOrFirst();

            tPrev = prev.getPageNumber() == first.getPageNumber()
                    ? tFirst
                    : "P" + prev.getPageNumber() + "S" + prev.getPageSize();
        }

        String tNext = null;
        if (pageContent.hasNext()) {
            Pageable next = pageable.next();
            tNext = "P" + next.getPageNumber() + "S" + next.getPageSize();
        }
        return Pagination.of(tFirst, tPrev, tNext);
    }

    @Override
    public Pageable getPageable(ServerRequest req, int size) {
        String page = req.param("page").orElseGet(() -> req.headers().firstHeader("page"));
        if (Objects.isNull(page) || !PAGE_PATTERN.matcher(page).matches()) {
            Integer pageSize = req.param("size").map(Integer::parseInt).orElse(size);
            return PageRequest.of(0, pageSize, Sort.by("id"));
        }

        int indexOfS = page.indexOf("S");
        int pageNumber = Integer.parseInt(page.substring(1, indexOfS));
        int pageSize = Integer.parseInt(page.substring(indexOfS + 1));
        return PageRequest.of(pageNumber, pageSize, Sort.by("id"));
    }
}
