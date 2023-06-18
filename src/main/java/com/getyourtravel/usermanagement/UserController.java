package com.getyourtravel.usermanagement;

import com.getyourtravel.entities.role.Role;
import com.getyourtravel.entities.role.RoleService;
import com.getyourtravel.entities.user.User;
import com.getyourtravel.entities.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private RoleService roleService;
    @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        return "redirect:/Imi-VIP";
    }

    @GetMapping("/Imi-VIP")
    public String vip(Principal principal) {

        String username = principal.getName();

        if (username.equals("Imi")) {
            return "bro";
        } else {
            return "redirect:/cities";
        }
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "access_denied";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration(@ModelAttribute User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "registration";
        }
        return "redirect:/cities";
    }

    @PostMapping("/registration")
    public String registration(@Valid User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User userInDB = userService.findByUsername(user.getUsername());
        if (userInDB != null) {
            model.addAttribute("message", "This username is already exists, please choose another");
            model.addAttribute("alertClass", "alert-danger");
            return "registration";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            return "registration";
        }

        if (! user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("message", "There are errors");
            model.addAttribute("alertClass", "alert-danger");
            model.addAttribute("passwordMatchProblem", "Passwords do not match");
            return "registration";
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role role = roleService.findByName("USER");
        roles.add(role);
        user.setRoles(roles);
        userService.save(user);
        redirectAttributes.addFlashAttribute("message", "User registration was success, you can now log in");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        return "redirect:/login";
    }
}