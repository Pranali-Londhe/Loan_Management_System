package com.example.loan_management_system.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class EmiCalculatorService {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);

    public BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || months <= 0) {
            throw new IllegalArgumentException("Principal must be positive and tenure must be greater than zero");
        }
        BigDecimal p = principal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal rate = annualRate == null ? BigDecimal.ZERO : annualRate;

        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return p.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(1200), MC);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal factor = onePlusR.pow(months, MC);

        BigDecimal numerator = p.multiply(monthlyRate, MC).multiply(factor, MC);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE, MC);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalPayable(BigDecimal monthlyEmi, int months) {
        return monthlyEmi.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalInterest(BigDecimal totalPayable, BigDecimal principal) {
        BigDecimal interest = totalPayable.subtract(principal).setScale(2, RoundingMode.HALF_UP);
        return interest.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : interest;
    }
}