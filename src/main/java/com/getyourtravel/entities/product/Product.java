package com.getyourtravel.entities.product;

import com.getyourtravel.Auditable;
import com.getyourtravel.entities.category.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Size(min = 2, message = "Product name must be at least 2 characters long")
    private String name;

    @Min(value = 0, message = "Price can not be negative")
    private double price;

    private String image;

    private String description;

    @Min(value = 0, message = "Sorting value can not be negative")
    private int sorting;

    @ManyToOne
    @JoinColumn(name = "category_id", updatable = false, insertable = false)
    private Category category;

    @Column(name = "category_id")
    private long categoryId;
}