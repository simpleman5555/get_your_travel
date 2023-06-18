package com.getyourtravel.entities.continent;

import com.getyourtravel.entities.city.City;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "continent")
@Getter
@Setter
public class Continent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String continent;

    @OneToMany(mappedBy = "continent")
    private List<City> cities;
}