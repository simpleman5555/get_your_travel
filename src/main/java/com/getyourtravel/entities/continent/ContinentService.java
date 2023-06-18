package com.getyourtravel.entities.continent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContinentService {

    @Autowired private ContinentRepository continentRepository;

    public Continent findById(long id) {
        return continentRepository.findById(id);
    }

    public List<Continent> findAll() {
        return continentRepository.findAll();
    }
}