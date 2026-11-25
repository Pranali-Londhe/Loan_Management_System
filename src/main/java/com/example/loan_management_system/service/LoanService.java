package com.example.loan_management_system.service;

import com.example.loan_management_system.dto.LoanApplicationRequest;
import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.entity.RepaymentSchedule;
import com.example.loan_management_system.entity.User;
import com.example.loan_management_system.enums.LoanStatus;
import com.example.loan_management_system.enums.Role;
import com.example.loan_management_system.repository.LoanRepository;
import com.example.loan_management_system.repository.RepaymentRepository;
import com.example.loan_management_system.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final EmiCalculatorService emiCalculatorService;
    private final PasswordEncoder passwordEncoder;

    public LoanService(UserRepository userRepository,
                       LoanRepository loanRepository,
                       RepaymentRepository repaymentRepository,
                       EmiCalculatorService emiCalculatorService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.emiCalculatorService = emiCalculatorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerBorrower(String fullName, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.BORROWER);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    @Transactional
    public Loan applyForLoan(String borrowerEmail, LoanApplicationRequest request) {
        User borrower = getUserByEmail(borrowerEmail);

        long activeCount = loanRepository.countByBorrowerAndStatusIn(
                borrower, List.of(LoanStatus.PENDING, LoanStatus.APPROVED, LoanStatus.ACTIVE));
        if (activeCount >= 3) {
            throw new IllegalStateException("You already have 3 open or active loans. Please clear an existing loan first.");
        }

        BigDecimal principal = request.getPrincipalAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal rate = request.getAnnualInterestRate().setScale(2, RoundingMode.HALF_UP);
        int months = request.getTenureMonths();

        BigDecimal emi = emiCalculatorService.calculateMonthlyEmi(principal, rate, months);
        BigDecimal totalPayable = emiCalculatorService.calculateTotalPayable(emi, months);
        BigDecimal totalInterest = emiCalculatorService.calculateTotalInterest(totalPayable, principal);

        Loan loan = new Loan();
        loan.setBorrower(borrower);
        loan.setPrincipalAmount(principal);
        loan.setAnnualInterestRate(rate);
        loan.setTenureMonths(months);
        loan.setMonthlyEmi(emi);
        loan.setTotalInterest(totalInterest);
        loan.setTotalPayable(totalPayable);
        loan.setRemainingBalance(totalPayable);
        loan.setPurpose(request.getPurpose().trim());
        loan.setStatus(LoanStatus.PENDING);

        return loanRepository.save(loan);
    }

    public List<Loan> getLoansForBorrower(String borrowerEmail) {
        return loanRepository.findByBorrowerOrderByAppliedAtDesc(getUserByEmail(borrowerEmail));
    }

    public Loan getLoanForUser(Long loanId, String email) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: #" + loanId));
        User user = getUserByEmail(email);
        if (user.getRole() != Role.ADMIN && !loan.getBorrower().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to view this loan.");
        }
        return loan;
    }

    public List<RepaymentSchedule> getScheduleForLoan(Long loanId, String email) {
        Loan loan = getLoanForUser(loanId, email);
        return repaymentRepository.findByLoanOrderByInstallmentNumberAsc(loan);
    }

    public List<Loan> getLoansByStatus(LoanStatus status) {
        return loanRepository.findByStatusOrderByAppliedAtAsc(status);
    }

    public Map<String, Object> getAdminSummary() {
        List<Loan> allLoans = loanRepository.findAll();
        BigDecimal totalLent = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.CLOSED)
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLent", totalLent);
        summary.put("pendingCount", loanRepository.countByStatus(LoanStatus.PENDING));
        summary.put("approvedCount", loanRepository.countByStatus(LoanStatus.APPROVED));
        summary.put("activeCount", loanRepository.countByStatus(LoanStatus.ACTIVE));
        summary.put("closedCount", loanRepository.countByStatus(LoanStatus.CLOSED));
        return summary;
    }

    @Transactional
    public void approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: #" + loanId));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be approved.");
        }
        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);
    }

    @Transactional
    public void rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: #" + loanId));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be rejected.");
        }
        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);
    }

    @Transactional
    public void disburseLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: #" + loanId));
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan must be APPROVED before disbursement.");
        }

        LocalDate startDate = LocalDate.now();
        loan.setDisbursedDate(startDate);
        loan.setStatus(LoanStatus.ACTIVE);

        BigDecimal remainingPrincipal = loan.getPrincipalAmount();
        BigDecimal monthlyRate = loan.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal emi = loan.getMonthlyEmi();

        List<RepaymentSchedule> items = new ArrayList<>();
        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            RepaymentSchedule item = new RepaymentSchedule();
            item.setLoan(loan);
            item.setInstallmentNumber(i);
            item.setDueDate(startDate.plusMonths(i));

            BigDecimal interestPart = remainingPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interestPart).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            if (i == loan.getTenureMonths() || principalPart.compareTo(remainingPrincipal) > 0) {
                principalPart = remainingPrincipal.setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal installmentTotal = principalPart.add(interestPart).setScale(2, RoundingMode.HALF_UP);

            item.setPrincipalComponent(principalPart);
            item.setInterestComponent(interestPart);
            item.setInstallmentAmount(installmentTotal);
            item.setPaid(false);

            remainingPrincipal = remainingPrincipal.subtract(principalPart).max(BigDecimal.ZERO);
            items.add(item);
        }

        repaymentRepository.saveAll(items);
        loanRepository.save(loan);
    }

    @Transactional
    public void payInstallment(Long loanId, Long installmentId, String borrowerEmail) {
        Loan loan = getLoanForUser(loanId, borrowerEmail);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Installments can only be paid on ACTIVE loans.");
        }

        RepaymentSchedule item = repaymentRepository.findById(installmentId)
                .orElseThrow(() -> new NoSuchElementException("Installment not found: #" + installmentId));

        if (!item.getLoan().getId().equals(loan.getId())) {
            throw new IllegalArgumentException("Installment does not belong to this loan.");
        }
        if (item.isPaid()) {
            throw new IllegalStateException("Installment #" + item.getInstallmentNumber() + " is already paid.");
        }

        item.setPaid(true);
        item.setPaidDate(LocalDate.now());
        repaymentRepository.save(item);

        BigDecimal updatedBalance = loan.getRemainingBalance()
                .subtract(item.getInstallmentAmount())
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        loan.setRemainingBalance(updatedBalance);

        boolean allPaid = repaymentRepository.findByLoanOrderByInstallmentNumberAsc(loan)
                .stream()
                .allMatch(RepaymentSchedule::isPaid);
        if (allPaid || updatedBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setRemainingBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            loan.setStatus(LoanStatus.CLOSED);
        }

        loanRepository.save(loan);
    }
}