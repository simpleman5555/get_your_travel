package com.getyourtravel.webshopmanagement;

import com.getyourtravel.entities.category.Category;
import com.getyourtravel.entities.category.CategoryService;
import com.getyourtravel.entities.product.Product;
import com.getyourtravel.entities.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Value("${pagination.product-elements-per-page}") private int numOfElementsPerPage;

    @ModelAttribute
    protected void sharedData(Model model) {
        List<Category> categoryList = categoryService.findAllBySorting();
        model.addAttribute("categories", categoryList);
        model.addAttribute("webshopIndex", true);
    }

    @GetMapping
    public String productIndex(Model model) {
        return handleCategoryNavigationAndPagination("all", 1, model);
    }

    @GetMapping("/{categoryName}/page/{pageNumber}")
    public String handleCategoryNavigationAndPagination(@PathVariable String categoryName,
                                           @PathVariable("pageNumber") int currentPage,
                                           Model model) {

        Page<Product> page = null;
        long categoryId = 0;
        if (! categoryName.equals("all")) categoryId = categoryService.findByCategoryName(categoryName).getId();

        if (categoryName.equals("all")) {
            page = productService.findPage(currentPage, numOfElementsPerPage, 0);
            model.addAttribute("categoryNameOfAll", "All products");
        } else {
            page = productService.findPage(currentPage, numOfElementsPerPage, categoryId);
        }

        if (currentPage > 1) {
            int allPages, allProductsSize = 0;
            if (categoryName.equals("all")) {
                allProductsSize = productService.findAll().size();
            } else {
                allProductsSize = productService.findByCategoryId(categoryId).size();
            }
            allPages = allProductsSize / numOfElementsPerPage;
            if (allProductsSize % numOfElementsPerPage != 0) allPages++;
            totalPages = allPages;
        }

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("products", productList);
        model.addAttribute("totalPages", totalPages);

        return "webshopmanagement/webshop";
    }

    @GetMapping("/{categoryName}")
    public String handleProductElementsPerPage(@PathVariable String categoryName,
                                               @RequestParam("pageNumber") int currentPage,
                                               @RequestParam int productElementsPerPage,
                                               Model model) {

        Page<Product> page = null;

        if (categoryName.equals("all")) {
            page = productService.findPage(currentPage, productElementsPerPage, 0);
        } else {
            long categoryId = categoryService.findByCategoryName(categoryName).getId();
            page = productService.findPage(currentPage, productElementsPerPage, categoryId);
        }

        model.addAttribute("products", productList);

        return "webshopmanagement/products_per_page";
    }

    @GetMapping("/cart/add/{id}")
    public String addItem(@PathVariable long id,
                          @RequestParam(value = "cartFull", required = false) String cartFull,
                          HttpSession httpSession,
                          Model model) {

        Product product = productService.findById(id);

        if (httpSession.getAttribute("cart") == null) {
            HashMap<Long, Cart> cart = new HashMap<>();
            cart.put(id, new Cart(id, product.getName(), String.valueOf(product.getPrice()), 1, product.getImage()));
        } else {
            HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");
            if (cart.containsKey(id)) {
                cart.put(id, new Cart(id, product.getName(), String.valueOf(product.getPrice()), ++quantity, product.getImage()));
            } else {
                cart.put(id, new Cart(id, product.getName(), String.valueOf(product.getPrice()), 1, product.getImage()));
            }
        }

        int quantity = 0;
        double priceSum = 0;

        HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");

        for (Cart cartElement : cart.values()) {
            quantity += cartElement.getQuantity();
            priceSum += cartElement.getQuantity() * Double.parseDouble(cartElement.getPrice());
        }

        model.addAttribute("quantity", quantity);
        model.addAttribute("priceSum", priceSum);

        return "webshopmanagement/cart_partial_dynamic";
    }

    @GetMapping("/cart/subtract/{id}")
    public String subtractFromProduct(@PathVariable long id,
                                      HttpSession httpSession,
                                      HttpServletRequest httpServletRequest) {

        Product product = productService.findById(id);
        HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");

        int quantity = cart.get(id).getQuantity();

        if (quantity == 1) {
            cart.remove(id);
        } else {
            cart.put(id, new Cart(id, product.getName(), String.valueOf(product.getPrice()), --quantity, product.getImage()));
        }

        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }

    @GetMapping("/cart/remove/{id}")
    public String removeProduct(@PathVariable long id,
                                HttpSession httpSession,
                                HttpServletRequest httpServletRequest) {

        HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");
        cart.remove(id);

        if (cart.size() == 0) {
            httpSession.removeAttribute("cart");
        }

        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }

    @GetMapping("/cart/clear")
    public String clearCart(HttpSession httpSession, HttpServletRequest httpServletRequest) {
        httpSession.removeAttribute("cart");
        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }

    @GetMapping("/cart/view")
    public String viewCart(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("cart") == null) {
            return "redirect:/products";
        }
        HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");
        model.addAttribute("cart", cart);
        return "webshopmanagement/cart";
    }

    @GetMapping("/cart/checkout")
    public String checkoutOfCart(HttpSession httpSession, RedirectAttributes redirectAttributes) {
        httpSession.removeAttribute("cart");
        redirectAttributes.addFlashAttribute("checkoutOfCart", "Thank you for your purchase!");
        return "redirect:/products";
    }
}