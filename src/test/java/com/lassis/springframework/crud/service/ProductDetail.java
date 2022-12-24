package com.lassis.springframework.crud.service;

import com.lassis.springframework.crud.entity.WithId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@Entity
@Table(name = "product_detail")
public class ProductDetail implements WithId<Long> {
    @Id
    @GeneratedValue
    private Long id;
    private String detail;
    @ManyToOne(optional = false)
    private Product product;
}
