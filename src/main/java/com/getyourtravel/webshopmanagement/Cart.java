package com.getyourtravel.webshopmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Cart {

    private long id;
    private String name;
    private String price;
    private int quantity;
    private String image;
}
