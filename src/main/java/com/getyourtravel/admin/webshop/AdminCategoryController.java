package com.getyourtravel.admin.webshop;

import com.getyourtravel.entities.category.Category;
import com.getyourtravel.entities.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired private CategoryService categoryService;
    @Value("${pagination.category-elements-per-page}") private int numOfElementsPerPage;

    @ModelAttribute
    protected void sharedData(Model model) {
        List<Category> categoryList = categoryService.findAll();
        model.addAttribute("categories", categoryList);
    }

    @GetMapping
    public String categoryIndex(Model model) {
        return getOnePageWithSorting(1, "sorting", "asc", false, model);
    }

    @GetMapping("/addCategory")
    public String addCategory(@ModelAttribute Category category) {
        return "admin/webshop/categories/add_category";
    }

    @PostMapping("/addCategory")
    public String addCategory(@Valid Category category,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/categories/add_category";
        }

        Category categoryExists = categoryService.findByCategoryName(category.getName());

        if (categoryExists != null) {
            model.addAttribute("message", "This category is already exists, please give another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/categories/add_category";
        } else {
            Integer newSortingValue = categoryService.getLastSortingValue();
            if (newSortingValue == null || newSortingValue < 1) {
                newSortingValue = 1;
            } else {
                newSortingValue++;
            }
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("message", "New category added");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            return "redirect:/admin/categories/addCategory";
        }
    }

    @GetMapping("/editCategory/{id}")
    public String editCategory(@ModelAttribute Category category,
                               @PathVariable long id,
                               Model model) {

        Category categoryInDB = categoryService.findById(id);
        model.addAttribute("category", categoryInDB);
        return "admin/webshop/categories/edit_category";
    }

    @PostMapping ("/editCategory")
    public String editCategory(@Valid Category category,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        Category categoryInDB = categoryService.findById(category.getId());
        model.addAttribute("nameOfExistingCategory", categoryInDB.getName());

        Category categoryExists = categoryService.findByCategoryNameANdIdNot(category.getName(), category.getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/categories/edit_category";
        }

        if (categoryExists != null) {
            model.addAttribute("message", "This category is already exists, please give another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/categories/edit_category";
        } else {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("message", "Category modified");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }

        return "redirect:/admin/categories/editCategory/" + category.getId();
    }

    @RequestMapping(value = "/delete/{id}/page/{pageNumber}", method = { RequestMethod.GET, RequestMethod.DELETE })
    public String deleteCategory(@PathVariable long id,
                                 @PathVariable("pageNumber") int currentPage,
                                 @RequestParam String field,
                                 @RequestParam("sortDir") String sortDirection,
                                 Model model) {

        categoryService.manageSortingValuesIfDeleteCategory(id, field, sortDirection);
        return getOnePageWithSorting(currentPage, field, sortDirection, true, model);
    }

    @GetMapping("/page/{pageNumber}/{field}")
    public String afterCategoryDelete(@PathVariable("pageNumber") int currentPage,
                                      @PathVariable String field,
                                      @RequestParam("sortDir") String sortDirection,
                                      Model model) {

        model.addAttribute("message", "Category successfully deleted");
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
            return "redirect:/admin/categories/page/" + currentPage + "/sorting?sortDir=asc";
        }

        Page<Category> page = categoryService.findPageWithSorting(field, sortDirection, currentPage, numOfElementsPerPage);
        List<Category> categoryList = page.getContent();
        int totalPages = page.getTotalPages();
        long totalCategories = page.getTotalElements();

        model.addAttribute("categories", categoryList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("field", field);
        model.addAttribute("sortDir", sortDirection.equalsIgnoreCase("asc") ? "desc" : "asc");

        return "admin/webshop/categories/category_index";
    }

    // so on laptop size - when we can use a mouse, so not on tablet or mobile - on the Admin section of Categories we can click and hold the mouse on a category,
    // and move it up or down causing the reorder of the categories
    @PostMapping("/reorder/{pageNumber}")
    public String reorder(@PathVariable("pageNumber") int currentPage,
                          @RequestParam String field,
                          @RequestParam("sortDir") String sortDirection,
                          Model model) {

        categoryService.manageSortingValuesIfReorderPage(ids, currentPage, field, sortDirection, numOfElementsPerPage);
        return getOnePageWithSorting(1, "sorting", "asc", false, model);
    }
}