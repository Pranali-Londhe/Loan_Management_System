package com.example.loan_management_system.controller;

import com.example.loan_management_system.dto.LoanApplicationRequest;
import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.enums.LoanStatus;
import com.example.loan_management_system.service.LoanService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/borrower")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        List<Loan> loans = loanService.getLoansForBorrower(principal.getName());
        long activeLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        long pendingLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.PENDING).count();
        long closedLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.CLOSED).count();

        model.addAttribute("user", loanService.getUserByEmail(principal.getName()));
        model.addAttribute("totalLoans", loans.size());
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("pendingLoans", pendingLoans);
        model.addAttribute("closedLoans", closedLoans);
        model.addAttribute("recentLoans", loans.stream().limit(5).toList());
        return "borrower/dashboard";
    }

    @GetMapping("/apply")
    public String applyForm(Model model) {
        if (!model.containsAttribute("loanRequest")) {
            model.addAttribute("loanRequest", new LoanApplicationRequest());
        }
        return "borrower/apply-loan";
    }

    @PostMapping("/apply")
    public String submitApplication(@Valid @ModelAttribute("loanRequest") LoanApplicationRequest request,
                                    BindingResult bindingResult,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "borrower/apply-loan";
        }
        try {
            Loan created = loanService.applyForLoan(principal.getName(), request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Loan application #" + created.getId() + " submitted for review.");
            return "redirect:/borrower/loans";
        } catch (IllegalStateException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "borrower/apply-loan";
        }
    }

    @GetMapping("/loans")
    public String myLoans(Principal principal, Model model) {
        model.addAttribute("loans", loanService.getLoansForBorrower(principal.getName()));
        return "borrower/my-loans";
    }

    @GetMapping("/loans/{id}")
    public String loanDetails(@PathVariable Long id, Principal principal, Model model) {
        Loan loan = loanService.getLoanForUser(id, principal.getName());
        model.addAttribute("loan", loan);
        model.addAttribute("schedules", loanService.getScheduleForLoan(id, principal.getName()));
        return "borrower/loan-details";
    }

    @PostMapping("/loans/{loanId}/installments/{installmentId}/pay")
    public String payInstallment(@PathVariable Long loanId,
                                 @PathVariable Long installmentId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        loanService.payInstallment(loanId, installmentId, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Installment payment recorded.");
        return "redirect:/borrower/loans/" + loanId;
    }
}