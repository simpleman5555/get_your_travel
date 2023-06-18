package com.getyourtravel.entities.city;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    City findById(long id);
    City findByCityNameAndCountryId(String city, long countryId);
    City findByCityNameAndCountryIdAndIdNot(String city, long countryId, long id);
    void deleteById(long id);
    City findByCityName(String cityName);
    ArrayList<City> findByCountryId(long countryId);
    ArrayList<City> findByContinentId(long continentId);
    ArrayList<City> findByLanguageId(long languageId);

    @Query(value = "SELECT * FROM City c" +
            " ORDER BY c.sorting ASC", nativeQuery = true)
    Page<City> findAllBySorting(Pageable pageable);

    @Query(value = "SELECT * FROM City c" +
            " WHERE c.most_popular = 'true'", nativeQuery = true)
    List<City> getMosPopularCities();

    @Query(value = "SELECT c.sorting FROM City c" +
            " ORDER BY c.sorting DESC LIMIT 1", nativeQuery = true)
    Integer getLastSortingValue();
}