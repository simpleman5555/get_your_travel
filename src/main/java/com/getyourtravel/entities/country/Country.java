package com.getyourtravel.entities.country;

import com.getyourtravel.entities.city.City;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "country")
@Getter
@Setter
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String country;

    @OneToMany(mappedBy = "country")
    private List<City> cities;

    public Country(String country) {
        this.country = country;
    }
}