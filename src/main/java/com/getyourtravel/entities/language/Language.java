package com.getyourtravel.entities.language;

import com.getyourtravel.entities.city.City;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "language")
@Getter
@Setter
public class Language implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String language;

    @OneToMany(mappedBy = "language")
    private List<City> cities;

    public Language(String language) {
        this.language = language;
    }
}