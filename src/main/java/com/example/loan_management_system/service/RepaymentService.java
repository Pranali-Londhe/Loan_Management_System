package com.example.loan_management_system.service;

import com.example.loan_management_system.dto.RepaymentResponse;
import com.example.loan_management_system.entity.Repayment;
import com.example.loan_management_system.enums.RepaymentStatus;
import com.example.loan_management_system.repository.RepaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;

    public RepaymentService(RepaymentRepository repaymentRepository) {
        this.repaymentRepository = repaymentRepository;
    }

    @Transactional
    public List<RepaymentResponse> generateSchedule(Long loanId, BigDecimal totalPayable, int tenureMonths, LocalDate startDate) {
        if (tenureMonths <= 0 || totalPayable == null || totalPayable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid loan repayment parameters");
        }

        LocalDate baseDate = startDate != null ? startDate : LocalDate.now();
        BigDecimal installment = totalPayable.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);

        List<Repayment> records = new ArrayList<>();
        for (int i = 1; i <= tenureMonths; i++) {
            Repayment r = new Repayment();
            r.setLoanId(loanId);
            r.setInstallmentNumber(i);
            r.setDueDate(baseDate.plusMonths(i));
            r.setAmountDue(installment);
            r.setAmountPaid(BigDecimal.ZERO);
            r.setStatus(RepaymentStatus.PENDING);
            records.add(r);
        }

        return repaymentRepository.saveAll(records).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RepaymentResponse> getRepaymentsByLoanId(Long loanId) {
        markOverdueIfPastDue();
        return repaymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RepaymentResponse payRepayment(Long repaymentId) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new NoSuchElementException("Repayment not found: " + repaymentId));

        if (repayment.getStatus() == RepaymentStatus.PAID) {
            throw new IllegalStateException("Repayment already paid");
        }

        repayment.setAmountPaid(repayment.getAmountDue());
        repayment.setPaidDate(LocalDate.now());
        repayment.setStatus(RepaymentStatus.PAID);

        return toResponse(repaymentRepository.save(repayment));
    }

    @Transactional
    public void markOverdueIfPastDue() {
        List<Repayment> overdueList = repaymentRepository.findByStatusAndDueDateBefore(
                RepaymentStatus.PENDING, LocalDate.now());
        for (Repayment r : overdueList) {
            r.setStatus(RepaymentStatus.OVERDUE);
        }
        if (!overdueList.isEmpty()) {
            repaymentRepository.saveAll(overdueList);
        }
    }

    private RepaymentResponse toResponse(Repayment r) {
        return new RepaymentResponse(
                r.getId(),
                r.getLoanId(),
                r.getInstallmentNumber(),
                r.getDueDate(),
                r.getAmountDue(),
                r.getAmountPaid(),
                r.getStatus(),
                r.getPaidDate()
        );
    }
}
