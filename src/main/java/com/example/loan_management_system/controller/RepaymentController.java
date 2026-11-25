package com.example.loan_management_system.controller;

import com.example.loan_management_system.dto.RepaymentResponse;
import com.example.loan_management_system.service.RepaymentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<RepaymentResponse>> generate(
            @RequestParam Long loanId,
            @RequestParam BigDecimal totalPayable,
            @RequestParam int tenureMonths,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(repaymentService.generateSchedule(loanId, totalPayable, tenureMonths, startDate));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<RepaymentResponse>> getByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(repaymentService.getRepaymentsByLoanId(loanId));
    }

    @PostMapping("/{repaymentId}/pay")
    public ResponseEntity<RepaymentResponse> pay(@PathVariable Long repaymentId) {
        return ResponseEntity.ok(repaymentService.payRepayment(repaymentId));
    }
}
