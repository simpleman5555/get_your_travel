package com.getyourtravel.entities.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Service
public class CityService {

    @Autowired private CityRepository cityRepository;

    @PersistenceContext private EntityManager entityManager;

    public City findById(long id) {
        return cityRepository.findById(id);
    }

    public List<City> findAll() {
        return cityRepository.findAll();
    }

    public City save(City city) {
        return cityRepository.save(city);
    }

    public void deleteCity(long id) {
        cityRepository.deleteById(id);
    }

    public City findByCityAndCountry(City city) {
        return cityRepository.findByCityNameAndCountryId(city.getCityName(), city.getCountryId());
    }

    public City findByCityNameAndCountryIdAndIdNot(String cityName, long countryId, long id) {
        return cityRepository.findByCityNameAndCountryIdAndIdNot(cityName, countryId, id);
    }

    public City findByCityName(String cityName) {
        return cityRepository.findByCityName(cityName);
    }

    public ArrayList<City> findByIdResultAsList(long cityId) {
        ArrayList<City> cityAsList = new ArrayList<City>();
        City city = cityRepository.findById(cityId);
        cityAsList.add(city);
        return cityAsList;
    }

    public ArrayList<City> findByCountryId(long countryId) {
        return cityRepository.findByCountryId(countryId);
    }

    public ArrayList<City> findByContinentId(long continentId) {
        return cityRepository.findByContinentId(continentId);
    }

    public ArrayList<City> findByLanguageId(long languageId) {
        return cityRepository.findByLanguageId(languageId);
    }

    public List<City> getMosPopularCities() {
        return cityRepository.getMosPopularCities();
    }

    public Integer getLastSortingValue() {
        return cityRepository.getLastSortingValue();
    }

    public Page<City> findPage(int pageNumber, int numOfElementsPerPage) {
        Pageable pageable = PageRequest.of(pageNumber - 1, numOfElementsPerPage);
        return cityRepository.findAllBySorting(pageable);
    }

    public Page<City> findPageWithSorting(String field, String sortDirection, int pageNumber, int numOfElementsPerPage) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, numOfElementsPerPage, sort);
        return cityRepository.findAll(pageable);
    }

    // here we handle the sorting values of Cities when we click and hold the mouse on a city and pull it up or down (reordering) - reordering works just with mouse,
    // so not on tablet or on mobile
    public void manageSortingValuesIfReorderPage(long[] ids, int pageNumber, String field, String sortDirection, int numOfElementsPerPage) {
        int count;
        int numOfElementsOfLastPage;
        int totalPages = findAll().size() / numOfElementsPerPage;

        if ((findAll().size() % numOfElementsPerPage) != 0) {
            totalPages++;
        }
        if ((findAll().size() % numOfElementsPerPage) == 0) {
            numOfElementsOfLastPage = numOfElementsPerPage;
        } else {
            numOfElementsOfLastPage = findAll().size() % numOfElementsPerPage;
        }

        // we add new sorting values to the cities which are on the current page depends on the sorting direction (descending, ascending)
        if (sortDirection.equals("desc")) {
            count = (totalPages * numOfElementsPerPage) - ((pageNumber - 1) * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            for (long id : ids) {
                City city = findById(id);
                city.setSorting(count);
                save(city);
                count--;
            }
        } else {
            count = (numOfElementsPerPage * (pageNumber - 1)) + 1;
            for (long id : ids) {
                City city = findById(id);
                city.setSorting(count);
                save(city);
                count++;
            }
        }

        // we add new sorting values to the cities which are before the current page depends on the sorting direction (descending, ascending)
        // this step is necessary just if the sorting field isn't "sorting" but something else
        if (pageNumber > 1) {
            int originalPageNumber = pageNumber;
            if (! field.equals("sorting")) {
                pageNumber--;
                for (int i = pageNumber; i > 0; i--) {

                    count = sortDirection.equals("asc") ?
                            (count - (2 * numOfElementsPerPage)) : (count + (2 * numOfElementsPerPage));

                    if (originalPageNumber == totalPages) {
                        count = sortDirection.equals("asc") ?
                                (count + (numOfElementsPerPage - numOfElementsOfLastPage)) :
                                (count - (numOfElementsPerPage - numOfElementsOfLastPage));
                    }

                    Page<City> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage);
                    List<City> cityList = page.getContent();
                    for (City city : cityList) {
                        city.setSorting(count);
                        save(city);
                        count = sortDirection.equals("asc") ? ++count : --count;
                    }
                }
                pageNumber = originalPageNumber;
            }
        }

        // we add new sorting values to the cities which are after the current page depends on the sorting direction (descending, ascending)
        if (totalPages > pageNumber) {
            if (sortDirection.equals("desc")) {
                count = (totalPages * numOfElementsPerPage) - (pageNumber * numOfElementsPerPage) - (numOfElementsPerPage - numOfElementsOfLastPage);
            } else {
                count = (numOfElementsPerPage * pageNumber) + 1;
            }
            pageNumber++;
            for (int i = pageNumber; i < totalPages + 1; i++) {
                Page<City> page = findPageWithSorting(field, sortDirection, i, numOfElementsPerPage);
                List<City> cityList = page.getContent();
                for (City city : cityList) {
                    city.setSorting(count);
                    save(city);
                    count = sortDirection.equals("asc") ? ++count : --count;
                }
            }
        }
    }

    // we have to add new sorting values to the cities if we delete a city and that's what we do here
    public void manageSortingValuesIfDeleteCity(long id, String field, String sortDirection) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<City> criteriaQuery = criteriaBuilder.createQuery(City.class);
        Root<City> root = criteriaQuery.from(City.class);
        criteriaQuery.select(root);
        criteriaQuery.orderBy(sortDirection.equals("asc") ? criteriaBuilder.asc(root.get(field)) : criteriaBuilder.desc(root.get(field)));

        List<City> cityList = entityManager.createQuery(criteriaQuery).getResultList();

        City cityForDeletion = findById(id);
        cityList.remove(cityForDeletion);

        int maxSortingValue = cityList.size();
        int newSortingValue = sortDirection.equals("asc") ? 1 : maxSortingValue;

        for (int i = 0; i < cityList.size(); i++) {
            City city = cityList.get(i);
            city.setSorting(newSortingValue);
            save(city);
            newSortingValue = sortDirection.equals("asc") ? ++newSortingValue : --newSortingValue;
        }
    }
}