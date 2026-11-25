package com.example.loan_management_system.repository;

import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.entity.User;
import com.example.loan_management_system.enums.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerOrderByAppliedAtDesc(User borrower);
    List<Loan> findByStatusOrderByAppliedAtAsc(LoanStatus status);
    long countByStatus(LoanStatus status);
    long countByBorrowerAndStatusIn(User borrower, List<LoanStatus> statuses);
}