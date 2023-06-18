package com.getyourtravel.admin.cities;

import com.getyourtravel.entities.city.City;
import com.getyourtravel.entities.city.CityService;
import com.getyourtravel.entities.continent.Continent;
import com.getyourtravel.entities.continent.ContinentService;
import com.getyourtravel.entities.country.Country;
import com.getyourtravel.entities.country.CountryService;
import com.getyourtravel.entities.language.Language;
import com.getyourtravel.entities.language.LanguageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.ss.usermodel.DataFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/cities")
@Slf4j
public class AdminCityController {

    @Autowired private CityService cityService;
    @Autowired private CountryService countryService;
    @Autowired private ContinentService continentService;
    @Autowired private LanguageService languageService;
    @Value("${pagination.city-elements-per-page}") private int numOfElementsPerPage;
    @Value("${upload.directory}") private String uploadDirectory;
    @Value("${media.directory}") private String mediaDirectory;

    @ModelAttribute
    protected void sharedData(Model model) {
        List<Country> countryList = countryService.findAll();
        List<Continent> continentList = continentService.findAll();
        List<Language> languages = languageService.findAll();
        model.addAttribute("countries", countryList);
        model.addAttribute("continents", continentList);
        model.addAttribute("languages", languages);
    }

    @GetMapping
    public String adminCityIndex(Model model) throws IOException {

        return getOnePageWithSorting(1, "sorting", "asc", false, model);
    }

    @GetMapping("/addCity")
    public String addCity(@ModelAttribute City city) {
        return "admin/cities/add_city";
    }

    @PostMapping("/addCity")
    public String addCity(@Valid City city,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          MultipartFile file,
                          HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/add_city";
        }

        City cityExists = cityService.findByCityAndCountry(city);
        String fileName = file.getOriginalFilename();
        boolean isFileOK = isImageFileOK(file);

        if (!isFileOK) {
            model.addAttribute("message", "Image must be a jpg or png");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/add_city";
        } else if (cityExists != null) {
            model.addAttribute("message", "This city of this country is already exists, please upload another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/add_city";
        } else {
            HttpSession session = request.getSession(false);
            Language language = languageService.findById(city.getLanguageId());
            city.setLanguage(language);
            city.setImage(fileName);
            Path newImagePath = Paths.get(uploadDirectory, fileName);
            Files.write(newImagePath, file.getBytes());
            Integer newSortingValue = cityService.getLastSortingValue();
            if (newSortingValue == null || newSortingValue < 1) {
                newSortingValue = 1;
            } else {
                newSortingValue++;
            }
            city.setSorting(newSortingValue);
            cityService.save(city);
            redirectAttributes.addFlashAttribute("message", "New city added. Image uploaded: " + fileName);
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }

        return "redirect:/admin/cities/addCity";
    }

    @GetMapping("/editCity/{id}")
    public String editCity(@PathVariable long id, Model model) {
        City city = cityService.findById(id);
        List mostPopularSelectOptions = new ArrayList<Boolean>() {
            {
                add(true);
                add(false);
            }
        };
        model.addAttribute("city", city);
        model.addAttribute("mostPopularSelectOptions", mostPopularSelectOptions);
        return "admin/cities/edit_city";
    }

    @PostMapping("/editCity")
    public String editCity(@Valid City city,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           MultipartFile file,
                           Model model,
                           HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/edit_city";
        }

        City cityInDB = cityService.findById(city.getId());
        model.addAttribute("cityName", cityInDB.getCityName());

        String fileName = file.getOriginalFilename();
        boolean newImageExists = fileName.contains(".");
        boolean isFileOK = newImageExists ? isImageFileOK(file) : true;

        City cityExists = cityService.findByCityNameAndCountryIdAndIdNot(city.getCityName(), city.getCountryId(), city.getId());

        if (cityExists != null || !isFileOK) {
            String countryOfSelect = countryService.findById(city.getCountryId()).getCountry();
            String continentOfSelect = continentService.findById(city.getContinentId()).getContinent();
            String languageOfSelect = languageService.findById(city.getLanguageId()).getLanguage();
            model.addAttribute("countryOfSelect", countryOfSelect);
            model.addAttribute("continentOfSelect", continentOfSelect);
            model.addAttribute("languageOfSelect", languageOfSelect);
        }

        if (cityExists != null) {
            model.addAttribute("message", "This city of this country is already exists, please give another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/edit_city";
        } else if (!isFileOK) {
            model.addAttribute("message", "Image must be a jpg or png");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/cities/edit_city";
        } else {
            if (newImageExists) {
                HttpSession session = request.getSession(false);
                String oldImage = cityService.findById(city.getId()).getImage();
                Path oldImagePath = Paths.get(uploadDirectory, oldImage);
                Files.deleteIfExists(oldImagePath);
                city.setImage(fileName);
                Path newImagePath = Paths.get(uploadDirectory, file.getOriginalFilename());
                Files.write(newImagePath, file.getBytes());
            }
            cityService.save(city);
            redirectAttributes.addFlashAttribute("message", "City successfully modified");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }

        return "redirect:/admin/cities/editCity/" + city.getId();
    }

    @RequestMapping(value = "/delete/{id}/page/{pageNumber}", method = {RequestMethod.GET, RequestMethod.DELETE})
    public String deleteCity(@PathVariable long id,
                             @PathVariable("pageNumber") int currentPage,
                             @RequestParam String field,
                             @RequestParam("sortDir") String sortDirection,
                             Model model) throws IOException {

        City city = cityService.findById(id);
        cityService.manageSortingValuesIfDeleteCity(id, field, sortDirection);
        cityService.deleteCity(id);
        return getOnePageWithSorting(currentPage, field, sortDirection, true, model);
    }

    @GetMapping("/page/{pageNumber}/{field}")
    public String afterCityDelete(@PathVariable("pageNumber") int currentPage,
                                      @PathVariable String field,
                                      @RequestParam("sortDir") String sortDirection,
                                      Model model) {

        model.addAttribute("message", "City successfully deleted");
        model.addAttribute("alertClass", "alert-success");
        return getOnePageWithSorting(currentPage, field, sortDirection, false, model);
    }

    @GetMapping("/page/{pageNumber}")
    public String getOnePageWithSorting(@PathVariable("pageNumber") int currentPage,
                                        @RequestParam String field,
                                        @RequestParam("sortDir") String sortDirection,
                                        boolean isDeletion,
                                        Model model) {

        if (isDeletion) {
            return "redirect:/admin/cities/page/" + currentPage + "/sorting?sortDir=asc";
        }

        Page<City> page = cityService.findPageWithSorting(field, sortDirection, currentPage, numOfElementsPerPage);
        List<City> cityList = page.getContent();
        int totalPages = page.getTotalPages();
        long totalCities = page.getTotalElements();
        if (totalPages == 0) {
            totalPages = 1;
        }
        model.addAttribute("cities", cityList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("field", field);
        model.addAttribute("sortDir", sortDirection.equalsIgnoreCase("asc") ? "desc" : "asc");

        return "admin/cities/admin_city_index";
    }

    // so on laptop size - when we can use a mouse, so not on tablet or mobile - on the Admin section of Cities we can click and hold the mouse on a city,
    // and move it up or down causing the reorder of the cities
    @PostMapping("/reorder/{pageNumber}")
    public String reorder(@RequestParam("id[]") long[] ids,
                          @PathVariable("pageNumber") int currentPage,
                          @RequestParam String field,
                          @RequestParam("sortDir") String sortDirection,
                          Model model) {

        cityService.manageSortingValuesIfReorderPage(ids, currentPage, field, sortDirection, numOfElementsPerPage);
        return getOnePageWithSorting(1, "sorting", "asc", false, model);
    }

    private boolean isImageFileOK(MultipartFile file) {
        boolean isFileOK = false;
        StringBuilder fileName = new StringBuilder();
        fileName.append(file.getOriginalFilename());
        if (fileName.toString().endsWith("jpg") || fileName.toString().endsWith("png")) {
            isFileOK = true;
        }
        return isFileOK;
    }
}