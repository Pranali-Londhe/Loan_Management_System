package com.example.loan_management_system.controller;

import com.example.loan_management_system.dto.LoanResponse;
import com.example.loan_management_system.dto.RejectRequest;
import com.example.loan_management_system.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
public class AdminLoanController {
    private final LoanService loanService;

    @GetMapping
    public List<LoanResponse> list(@RequestParam(required = false) String status) {
        return loanService.byStatus(status);
    }

    @PutMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id) {
        return loanService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public LoanResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest req) {
        return loanService.reject(id, req.remark());
    }

    @PutMapping("/{id}/disburse")
    public LoanResponse disburse(@PathVariable Long id) {
        return loanService.disburse(id);
    }
}
