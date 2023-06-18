package com.getyourtravel;

import com.getyourtravel.webshopmanagement.Cart;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;

@ControllerAdvice
public class Common {

    @ModelAttribute
    protected void sharedData(Model model, HttpSession httpSession, Principal principal) {

        String username = "Unknown";

        if (principal != null) {
            username = principal.getName();
        }

        boolean cartActive = false;

        if (httpSession.getAttribute("cart") != null) {
            HashMap<Long, Cart> cart = (HashMap<Long, Cart>) httpSession.getAttribute("cart");

            int quantity = 0;
            double priceSum = 0;

            for (Cart cartElement : cart.values()) {
                quantity += cartElement.getQuantity();
                priceSum += cartElement.getQuantity() * Double.parseDouble((cartElement.getPrice()));
            }

            model.addAttribute("quantity", quantity);
            model.addAttribute("priceSum", priceSum);
            cartActive = true;
        }

        model.addAttribute("cartActive", cartActive);
        model.addAttribute("loggedInUsername", username);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, IllegalStateException.class, SizeLimitExceededException.class, MultipartException.class})
    public String handleMaxSizeException(RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        redirectAttributes.addFlashAttribute("message", "Image file size is too large (max. 1 MB)");
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }

    @ExceptionHandler({ConstraintViolationException.class, DataIntegrityViolationException.class})
    public String handleConstraintViolationException(RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        redirectAttributes.addFlashAttribute("message", "One or more products belong to this category, so it is not possible to delete it until then");
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }
}