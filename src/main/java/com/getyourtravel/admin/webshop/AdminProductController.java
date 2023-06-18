package com.getyourtravel.admin.webshop;

import com.getyourtravel.entities.category.Category;
import com.getyourtravel.entities.category.CategoryService;
import com.getyourtravel.entities.product.Product;
import com.getyourtravel.entities.product.ProductService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Value("${pagination.product-elements-per-page}") private int numOfElementsPerPage;

    @Value("${upload.directory}")
    private String uploadDirectory;

    @ModelAttribute
    protected void sharedData(Model model) {
        List<Category> categoryList = categoryService.findAllBySorting();
        model.addAttribute("categories", categoryList);
        model.addAttribute("adminProductIndex", true);
    }

    @GetMapping
    public String productIndex(Model model) {
        return handleCategoryNavigationAndPagination("all", 1, "sorting", "asc", false, model);
    }

    @GetMapping("/addProduct")
    public String addProduct(@ModelAttribute Product product) {
        return "admin/webshop/products/add_product";
    }

    @PostMapping("/addProduct")
    public String addProduct(@Valid Product product,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             MultipartFile file,
                             HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/add_product";
        }

        Product productExists = productService.findByNameAndCategoryIdAndIdNot(product.getName(), product.getCategoryId(), product.getId());
        String fileName = file.getOriginalFilename();
        boolean isFileOK = isImageFileOK(file);

        if (!isFileOK) {
            model.addAttribute("message", "Image must be a jpg or png");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/add_product";
        } else if (productExists != null) {
            model.addAttribute("message", "This product is already exists, please give another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/add_product";
        } else {
            HttpSession session = request.getSession(false);
            product.setImage(fileName);
            Path newImagePath = Paths.get(uploadDirectory, fileName);
            Files.write(newImagePath, file.getBytes());
            Integer newSortingValue = productService.getLastSortingValue();
            if (newSortingValue == null || newSortingValue < 1) {
                newSortingValue = 1;
            } else {
                newSortingValue++;
            }
            productService.save(product);
            redirectAttributes.addFlashAttribute("message", "New product added. Image uploaded: " + fileName);
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }

        return "redirect:/admin/products/addProduct";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable long id,
                              @ModelAttribute Product product,
                              Model model) {

        Product productInDB =   productService.findById(product.getId());
        model.addAttribute("product", productInDB);

        return "admin/webshop/products/edit_product";
    }

    @PostMapping("/editProduct")
    public String editProduct(@Valid Product product,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              MultipartFile file,
                              HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/edit_product";
        }

        Product productInDB =   productService.findById(product.getId());
        model.addAttribute("nameOfExistingProduct", productInDB.getName());

        String fileName = file.getOriginalFilename();
        boolean newImageExists = fileName.contains(".");
        boolean isFileOK = newImageExists ? isImageFileOK(file) : true;

        Product productExists = productService.findByNameAndCategoryIdAndIdNot(product.getName(), product.getCategoryId(), product.getId());

        if (!isFileOK || productExists != null) {
            String categoryOfSelect = categoryService.findById(product.getCategoryId()).getName();
            model.addAttribute("categoryOfSelect", categoryOfSelect);
        }

        if (!isFileOK) {
            model.addAttribute("message", "Image must be a jpg or png");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/edit_product";
        } else if (productExists != null) {
            model.addAttribute("message", "This product is already exists, please give another");
            model.addAttribute("alertClass", "alert-danger");
            return "admin/webshop/products/edit_product";
        } else {
            if (newImageExists) {
                HttpSession session = request.getSession(false);
                String oldImage = productService.findById(product.getId()).getImage();
                Path oldImagePath = Paths.get(uploadDirectory, oldImage);
                product.setImage(fileName);
                Path newImagePath = Paths.get(uploadDirectory, fileName);
                Files.write(newImagePath, file.getBytes());
            }
            productService.save(product);
            redirectAttributes.addFlashAttribute("message", "Product successfully modified");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        }

        return "redirect:/admin/products/editProduct/" + product.getId();
    }

    @RequestMapping(value = "/delete/{id}/page/{pageNumber}", method = { RequestMethod.GET, RequestMethod.DELETE })
    public String deleteProduct(@PathVariable long id,
                                @PathVariable("pageNumber") int currentPage,
                                @RequestParam String field,
                                @RequestParam("sortDir") String sortDirection,
                                @RequestParam("category") String categoryName,
                                Model model) throws IOException {

        Product product = productService.findById(id);
        productService.manageSortingValuesIfDeleteProduct(id, field, sortDirection);
        productService.deleteProduct(id);
        String oldImage = product.getImage();
        Path oldImagePath = Paths.get(uploadDirectory, oldImage);
        return handleCategoryNavigationAndPagination(categoryName, currentPage, field, sortDirection, true, model);
    }

    @GetMapping("/{categoryName}/page/{pageNumber}/{field}")
    public String afterProductDelete(@PathVariable String categoryName,
                                     @PathVariable("pageNumber") int currentPage,
                                     @PathVariable String field,
                                     @RequestParam("sortDir") String sortDirection,
                                     Model model) {

        model.addAttribute("message", "Product successfully deleted");
        model.addAttribute("alertClass", "alert-success");
        return handleCategoryNavigationAndPagination(categoryName, currentPage, field, sortDirection, false, model);
    }

    @GetMapping("/{categoryName}/page/{pageNumber}")
    public String handleCategoryNavigationAndPagination(@PathVariable String categoryName,
                                        @PathVariable("pageNumber") int currentPage,
                                        @RequestParam String field,
                                        @RequestParam("sortDir") String sortDirection,
                                        boolean isDeletion,
                                        Model model) {

        if (isDeletion) {
            return "redirect:/admin/products/all/page/" + currentPage + "/sorting?sortDir=asc";
        }

        Page<Product> page = null;

        if (categoryName.equals("all")) {
            page = productService.findPageWithSorting(field, sortDirection, currentPage, numOfElementsPerPage, 0);
        } else {
            long categoryId = categoryService.findByCategoryName(categoryName).getId();
            page = productService.findPageWithSorting(field, sortDirection, currentPage, numOfElementsPerPage, categoryId);
        }

        List<Product> productList = page.getContent();
        int totalPages = page.getTotalPages();
        if (totalPages == 0) {
            totalPages = 1;
        }
        long totalProducts = page.getTotalElements();

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("products", productList);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("field", field);
        model.addAttribute("sortDir", sortDirection.equalsIgnoreCase("asc") ? "desc" : "asc");

        return "admin/webshop/products/product_index";
    }

    // so on laptop size - when we can use a mouse, so not on tablet or mobile - on the Admin section of Products we can click and hold the mouse on a product,
    // and move it up or down causing the reorder of the products
    @PostMapping("/reorder/{pageNumber}")
    public String reorder(@RequestParam("id[]") long[] ids,
                          @PathVariable("pageNumber") int currentPage,
                          @RequestParam String field,
                          @RequestParam("sortDir") String sortDirection,
                          Model model) {

        productService.manageSortingValuesIfReorderPage(ids, currentPage, field, sortDirection, numOfElementsPerPage);
        return handleCategoryNavigationAndPagination("all", 1, "sorting", "asc", false, model);
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