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
@Table(name = "product_detail_max")
public class ProductDetailMax implements WithId<Long> {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private ProductDetail productDetail;
}
