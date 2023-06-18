package com.getyourtravel.contact;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @GetMapping
    public String contactPage() {
        return "contact";
    }

    @PostMapping("/send")
    public String sendMessage(RedirectAttributes redirectAttributes,
                              HttpServletRequest httpServletRequest) {

        redirectAttributes.addFlashAttribute("contactMessage", "Thank you for your message!");
        String refererLink = httpServletRequest.getHeader("referer");
        return "redirect:" + refererLink;
    }
}