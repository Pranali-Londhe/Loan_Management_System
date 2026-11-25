package com.example.loan_management_system.repository;

import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.entity.RepaymentSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepaymentRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanOrderByInstallmentNumberAsc(Loan loan);
}