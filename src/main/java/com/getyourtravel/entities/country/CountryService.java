package com.getyourtravel.entities.country;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryService {

    @Autowired CountryRepository countryRepository;

    public Country findById(long id) {
        return countryRepository.findById(id);
    }

    public List<Country> findAll() {
        return countryRepository.findAll();
    };

    public List<Country> saveAll(List<Country> countryList) {
        return countryRepository.saveAll(countryList);
    }
}