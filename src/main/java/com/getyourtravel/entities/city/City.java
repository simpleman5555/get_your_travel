package com.getyourtravel.entities.city;

import com.getyourtravel.Auditable;
import com.getyourtravel.entities.continent.Continent;
import com.getyourtravel.entities.country.Country;
import com.getyourtravel.entities.language.Language;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "city")
public class City extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "image")
    private String image;

    @Column(name = "city")
    @Size(min = 2, message = "City name must be at least 2 characters long")
    private String cityName;

    @Column(name = "population")
    @Min(value = 1, message = "Population value must be minimum 1")
    private int population;

    @Column(name = "description")
    private String description;

    @Size(min = 18, message = "Video link must be at least 18 characters long")
    private String videoLink;

    @Column(name = "sorting")
    @Min(value = 0, message = "Sorting value can not be negative")
    private int sorting;

    @Column(name = "mostPopular")
    boolean mostPopular;

    @ManyToOne
    @JoinColumn(name = "language_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Language language;

    @Column(name = "language_id")
    private long languageId;

    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Country country;

    @Column(name = "country_id")
    private long countryId;

    @ManyToOne
    @JoinColumn(name = "continent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Continent continent;

    @Column(name = "continent_id")
    private long continentId;
}