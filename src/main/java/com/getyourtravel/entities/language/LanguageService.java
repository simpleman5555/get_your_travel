package com.getyourtravel.entities.language;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageService {

    @Autowired private LanguageRepository languageRepository;

    public Language findById(long id) {
        return languageRepository.findById(id);
    }

    public List<Language> findAll() {
        return languageRepository.findAll();
    };

    public List<Language> saveAll(List<Language> languageList) {
        return languageRepository.saveAll(languageList);
    }
}