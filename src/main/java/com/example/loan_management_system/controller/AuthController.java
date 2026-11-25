package com.example.loan_management_system.controller;

import com.example.loan_management_system.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final LoanService loanService;

    public AuthController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (fullName.isBlank() || email.isBlank() || password.length() < 6) {
            model.addAttribute("errorMessage", "Name and email are required, and password must be at least 6 characters.");
            return "auth/register";
        }
        try {
            loanService.registerBorrower(fullName, email, password);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful. Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }
}