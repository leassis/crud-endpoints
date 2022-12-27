package com.lassis.springframework.crud.repository;

import com.lassis.springframework.crud.service.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {}
