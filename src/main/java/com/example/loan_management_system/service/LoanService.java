package com.example.loan_management_system.service;

import com.example.loan_management_system.dto.LoanApplicationRequest;
import com.example.loan_management_system.dto.LoanResponse;
import com.example.loan_management_system.dto.RepaymentResponse;
import com.example.loan_management_system.entity.BorrowerProfile;
import com.example.loan_management_system.entity.Loan;
import com.example.loan_management_system.entity.RepaymentSchedule;
import com.example.loan_management_system.enums.LoanStatus;
import com.example.loan_management_system.enums.RepaymentStatus;
import com.example.loan_management_system.repository.BorrowerProfileRepository;
import com.example.loan_management_system.repository.LoanRepository;
import com.example.loan_management_system.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/*
 * Business rules: eligibility, apply, approve, reject, disburse, repay.
 * Loan life cycle:  PENDING -> APPROVED -> ACTIVE -> CLOSED      (or PENDING -> REJECTED)
 */
@Service
@RequiredArgsConstructor
public class LoanService {
    private static final double INTEREST_RATE = 12.0;
    private static final List<LoanStatus> OPEN =
            List.of(LoanStatus.PENDING, LoanStatus.APPROVED, LoanStatus.ACTIVE);

    private final LoanRepository loanRepo;
    private final BorrowerProfileRepository profileRepo;
    private final RepaymentRepository repaymentRepo;
    private final EmiCalculatorService emiCalc;

    // ================= borrower =================

    @Transactional
    public LoanResponse apply(Long userId, LoanApplicationRequest req) {
        BorrowerProfile p = profileOf(userId);

        if (loanRepo.existsByBorrowerIdAndStatusIn(p.getId(), OPEN))
            throw new IllegalStateException("You already have an active loan application");

        double emi = emiCalc.calculateEmi(req.amount(), INTEREST_RATE, req.tenureMonths());
        if (emi > p.getMonthlyIncome() * 0.5)
            throw new IllegalStateException("Monthly income too low for this EMI");

        Loan loan = new Loan();
        loan.setAmount(req.amount());
        loan.setInterestRate(INTEREST_RATE);
        loan.setTenureMonths(req.tenureMonths());
        loan.setEmi(emi);
        loan.setStatus(LoanStatus.PENDING);
        loan.setBorrower(p);
        return toResponse(loanRepo.save(loan));
    }

    public List<LoanResponse> myLoans(Long userId) {
        return loanRepo.findByBorrowerId(profileOf(userId).getId())
                .stream().map(this::toResponse).toList();
    }

    public LoanResponse details(Long loanId, Long userId) {
        return toResponse(ownLoan(loanId, userId));
    }

    public List<RepaymentResponse> schedule(Long loanId, Long userId) {
        ownLoan(loanId, userId);
        return repaymentRepo.findByLoanIdOrderByInstallmentNo(loanId)
                .stream().map(this::toRepaymentResponse).toList();
    }

    @Transactional
    public RepaymentResponse pay(Long repaymentId, Long userId) {
        RepaymentSchedule r = repaymentRepo.findById(repaymentId)
                .orElseThrow(() -> new NoSuchElementException("Repayment not found: " + repaymentId));
        Loan loan = ownLoan(r.getLoan().getId(), userId);

        if (r.getStatus() == RepaymentStatus.PAID)
            throw new IllegalStateException("Instalment already paid");
        if (loan.getStatus() != LoanStatus.ACTIVE)
            throw new IllegalStateException("Loan is not active - repayments start after disbursement");

        r.setStatus(RepaymentStatus.PAID);
        repaymentRepo.save(r);

        boolean allPaid = repaymentRepo.findByLoanIdOrderByInstallmentNo(loan.getId())
                .stream().allMatch(x -> x.getStatus() == RepaymentStatus.PAID);
        if (allPaid) {
            loan.setStatus(LoanStatus.CLOSED);
            loanRepo.save(loan);
        }
        return toRepaymentResponse(r);
    }

    // ================= admin =================

    // Streams filter, as the project requires
    public List<LoanResponse> byStatus(String status) {
        LoanStatus wanted = parseStatus(status);
        return loanRepo.findAll().stream()
                .filter(l -> wanted == null || l.getStatus() == wanted)
                .map(this::toResponse).toList();
    }

    @Transactional
    public LoanResponse approve(Long id) {
        Loan loan = loanInStatus(id, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.APPROVED);
        loanRepo.save(loan);
        generateSchedule(loan);
        return toResponse(loan);
    }

    public LoanResponse reject(Long id, String remark) {
        Loan loan = loanInStatus(id, LoanStatus.PENDING);
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRemark(remark);
        return toResponse(loanRepo.save(loan));
    }

    public LoanResponse disburse(Long id) {
        Loan loan = loanInStatus(id, LoanStatus.APPROVED);
        loan.setStatus(LoanStatus.ACTIVE);
        return toResponse(loanRepo.save(loan));
    }

    // ================= helpers =================

    private void generateSchedule(Loan loan) {
        List<RepaymentSchedule> rows = IntStream.rangeClosed(1, loan.getTenureMonths())
                .mapToObj(i -> {
                    RepaymentSchedule r = new RepaymentSchedule();
                    r.setInstallmentNo(i);
                    r.setDueDate(LocalDate.now().plusMonths(i));
                    r.setAmount(loan.getEmi());
                    r.setStatus(RepaymentStatus.PENDING);
                    r.setLoan(loan);
                    return r;
                }).toList();
        repaymentRepo.saveAll(rows);
    }

    private BorrowerProfile profileOf(Long userId) {
        return profileRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Borrower profile not found"));
    }

    // a borrower may only touch their own loans; others get 404 so we do not reveal it exists
    private Loan ownLoan(Long loanId, Long userId) {
        Loan loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: " + loanId));
        if (!loan.getBorrower().getUser().getId().equals(userId))
            throw new NoSuchElementException("Loan not found: " + loanId);
        return loan;
    }

    private Loan loanInStatus(Long id, LoanStatus expected) {
        Loan loan = loanRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Loan not found: " + id));
        if (loan.getStatus() != expected)
            throw new IllegalStateException("Loan is " + loan.getStatus() + ", expected " + expected);
        return loan;
    }

    private LoanStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return LoanStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status
                    + " (use PENDING, APPROVED, REJECTED, ACTIVE or CLOSED)");
        }
    }

    private LoanResponse toResponse(Loan l) {
        return new LoanResponse(l.getId(), l.getAmount(), l.getInterestRate(), l.getTenureMonths(),
                l.getEmi(),
                emiCalc.totalInterest(l.getAmount(), l.getEmi(), l.getTenureMonths()),
                emiCalc.totalPayable(l.getEmi(), l.getTenureMonths()),
                l.getStatus(), l.getRemark());
    }

    private RepaymentResponse toRepaymentResponse(RepaymentSchedule r) {
        return new RepaymentResponse(r.getId(), r.getInstallmentNo(),
                r.getDueDate(), r.getAmount(), r.getStatus());
    }
}
