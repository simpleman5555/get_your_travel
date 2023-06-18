package com.getyourtravel.citymanagement;

import com.getyourtravel.entities.login.Login;
import com.getyourtravel.entities.city.City;
import com.getyourtravel.entities.city.CityService;
import com.getyourtravel.entities.continent.Continent;
import com.getyourtravel.entities.continent.ContinentService;
import com.getyourtravel.entities.country.Country;
import com.getyourtravel.entities.country.CountryService;
import com.getyourtravel.entities.language.Language;
import com.getyourtravel.entities.language.LanguageService;
import com.getyourtravel.entities.login.LoginService;
import com.getyourtravel.entities.user.User;
import com.getyourtravel.entities.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cities")
@Slf4j
public class CityController {

    @Autowired private CityService cityService;
    @Autowired private CountryService countryService;
    @Autowired private ContinentService continentService;
    @Autowired private LanguageService languageService;
    @Autowired private UserService userService;
    @Autowired private LoginService loginService;

    @Value("${pagination.city-elements-per-page}")
    private int numOfElementsPerPage;

    @ModelAttribute
    protected void sharedData(Model model) {
        List<Country> countryList = countryService.findAll();
        List<Continent> continentList = continentService.findAll();
        List<Language> languageList = languageService.findAll();
        List<City> mostPopularCities = new ArrayList<City>();
        mostPopularCities = cityService.getMosPopularCities();
        model.addAttribute("citiesForSelect", cityService.findAll());
        model.addAttribute("countriesForSelect", countryList);
        model.addAttribute("continentsForSelect", continentList);
        model.addAttribute("languagesForSelect", languageList);
        model.addAttribute("mostPopularCities", mostPopularCities);
    }

    @GetMapping
    public String cityIndex(Model model, Principal principal) {

        Date currentDate = new Date();
        User user = userService.findByUsername(principal.getName());

        if (! user.getUsername().equals("Imi")) {
            user.setLastLogin(currentDate);
            userService.save(user);
            Login login = new Login(principal.getName(), currentDate);
            loginService.save(login);
        }

        Page<City> page  = cityService.findPage(1, numOfElementsPerPage);
        List<City> cityList = page.getContent();
        int totalPages = page.getTotalPages();
        if (totalPages == 0) {
            totalPages = 1;
        }
        model.addAttribute("dynamicallyChangeableCities", cityList);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", totalPages);

        return "citymanagement/city_index";
    }

    @GetMapping("/page/{pageNumber}")
    public String getOnePageWithPaginationAndFilter(@PathVariable("pageNumber") int currentPage,
                                                    @RequestParam Map<String,String> allParams,
                                                    Model model) {

        List<City> cityList = filterCities(allParams, numOfElementsPerPage);
        int totalPages;

        if (cityList.size() == 0) {
            if (allParams.get("cityId").equals("City") &&
                allParams.get("countryId").equals("Country") &&
                allParams.get("continentId").equals("Continent") &&
                allParams.get("languageId").equals("Language")) {

                int allCities = cityService.findAll().size();

                if ((allCities % numOfElementsPerPage) != 0) {
                    totalPages++;
                }
            } else {
                totalPages = 0;
            }
        } else {
            int allFilteredCities = cityList.size();
            totalPages = allFilteredCities / numOfElementsPerPage;

            if ((allFilteredCities % numOfElementsPerPage) != 0) {
                totalPages++;
            }

            ArrayList<City> finalCityList = new ArrayList<City>();
            int startIndex = (currentPage - 1) * numOfElementsPerPage;

            for (int i = startIndex; i < endSize; i++) {
                finalCityList.add(cityList.get(i));
            }
            cityList = finalCityList;
        }

        if (totalPages == 0) {
            totalPages = 1;
        }

        model.addAttribute("dynamicallyChangeableCities", cityList);
        model.addAttribute("totalPages", totalPages);

        return "citymanagement/city_index_citycards_section";
    }

    // here we get back the filtered cities
    // for this we collect separately all of the possible cities related to the fields (select) of the filter section on the page
    private ArrayList<City> filterCities(Map<String,String> allParams, int numOfElementsPerPage) {

        ArrayList<City> result = new ArrayList<City>();

        String cityId = allParams.get("cityId");
        String countryId = allParams.get("countryId");
        String continentId = allParams.get("continentId");
        String languageId = allParams.get("languageId");

        ArrayList<City> listForFilter = new ArrayList<City>();
        ArrayList<ArrayList<City>> arrayOfCityArrays = new ArrayList<ArrayList<City>>();

        if (cityId != null && ! cityId.equals("City")){
            listForFilter = cityService.findByIdResultAsList(Long.parseLong(cityId));
            arrayOfCityArrays.add(listForFilter);
        }
        if (countryId != null && ! countryId.equals("Country")) {
            listForFilter = cityService.findByCountryId(Long.parseLong(countryId));
            arrayOfCityArrays.add(listForFilter);
        }
        if (continentId != null && ! continentId.equals("Continent")) {
            listForFilter = cityService.findByContinentId(Long.parseLong(continentId));
            arrayOfCityArrays.add(listForFilter);
        }
        if (languageId != null && ! languageId.equals("Language")) {
            listForFilter = cityService.findByLanguageId(Long.parseLong(languageId));
            arrayOfCityArrays.add(listForFilter);
        }

        return result;
    }

    // here we compare the collected possible cities, because we would like to know whether there are any common cities of them or not
    private ArrayList<City> getFilteredCities(ArrayList<ArrayList<City>> arrayOfCityArrays, int numOfElementsPerPage) {
        List<City> result = new ArrayList<City>();
        int i = 0;
        List<City> cityArrayForCompare = arrayOfCityArrays.get(i);
        i++;
        do {
            if (arrayOfCityArrays.size() > 1) {
                List<City> cityArrayForCompare2 = arrayOfCityArrays.get(i);
                result = cityArrayForCompare
                        .stream()
                        .filter(cityArrayForCompare2::contains)
                        .collect(Collectors
                                .toList());

                cityArrayForCompare = result;
                i++;
            } else {
                result = cityArrayForCompare;
            }
        } while(i < arrayOfCityArrays.size());

        return new ArrayList<>(result);
    }
}