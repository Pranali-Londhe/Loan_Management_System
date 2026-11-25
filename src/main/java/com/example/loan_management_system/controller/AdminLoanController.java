package com.example.loan_management_system.controller;

import com.example.loan_management_system.enums.LoanStatus;
import com.example.loan_management_system.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminLoanController {

    private final LoanService loanService;

    public AdminLoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("summary", loanService.getAdminSummary());
        model.addAttribute("pendingLoans", loanService.getLoansByStatus(LoanStatus.PENDING));
        return "admin/dashboard";
    }

    @GetMapping("/loans/pending")
    public String pendingQueue(Model model) {
        model.addAttribute("pendingLoans", loanService.getLoansByStatus(LoanStatus.PENDING));
        model.addAttribute("approvedLoans", loanService.getLoansByStatus(LoanStatus.APPROVED));
        return "admin/pending-loans";
    }

    @PostMapping("/loans/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        loanService.approveLoan(id);
        redirectAttributes.addFlashAttribute("successMessage", "Loan #" + id + " approved.");
        return "redirect:/admin/loans/pending";
    }

    @PostMapping("/loans/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        loanService.rejectLoan(id);
        redirectAttributes.addFlashAttribute("successMessage", "Loan #" + id + " rejected.");
        return "redirect:/admin/loans/pending";
    }

    @PostMapping("/loans/{id}/disburse")
    public String disburse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        loanService.disburseLoan(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Loan #" + id + " disbursed and repayment schedule generated.");
        return "redirect:/admin/loans/pending";
    }
}