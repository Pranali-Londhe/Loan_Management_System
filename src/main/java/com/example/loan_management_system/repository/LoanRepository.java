
package com.example.loan_management_system.repository;

import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.entity.LoanStatus;
import com.example.loan_management_system.entity.BorrowerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBorrower(BorrowerProfile borrower);

    boolean existsByBorrowerAndStatus(
            BorrowerProfile borrower,
            LoanStatus status
    );
}
