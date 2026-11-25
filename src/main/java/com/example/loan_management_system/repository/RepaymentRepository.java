package com.example.loan_management_system.repository;

import com.example.loan_management_system.entity.Repayment;
import com.example.loan_management_system.enums.RepaymentStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
    List<Repayment> findByStatusAndDueDateBefore(RepaymentStatus status, LocalDate date);
}
