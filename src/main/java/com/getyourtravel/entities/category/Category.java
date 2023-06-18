package com.getyourtravel.entities.category;

import com.getyourtravel.Auditable;
import com.getyourtravel.entities.product.Product;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Size(min = 2, message = "Category name must be at least 2 characters long")
    private String name;

    @Min(value = 0, message = "Sorting value can not be negative")
    private int sorting;

    @OneToMany(mappedBy = "category")
    private List<Product> productList;
}