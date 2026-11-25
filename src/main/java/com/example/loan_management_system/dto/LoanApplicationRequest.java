package com.example.loan_management_system.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1,000")
    @DecimalMax(value = "5000000.00", message = "Maximum loan amount is 5,000,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "1.00", message = "Interest rate must be at least 1%")
    @DecimalMax(value = "40.00", message = "Interest rate cannot exceed 40%")
    private BigDecimal annualInterestRate = new BigDecimal("10.50");

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 120, message = "Maximum tenure is 120 months")
    private Integer tenureMonths = 12;

    @NotBlank(message = "Loan purpose is required")
    @Size(max = 250, message = "Purpose must be 250 characters or fewer")
    private String purpose;

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(BigDecimal annualInterestRate) { this.annualInterestRate = annualInterestRate; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}