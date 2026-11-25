package com.example.loan_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "borrower_profiles")
public class BorrowerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 20)
    private String panNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false)
    private String employmentType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public BorrowerProfile() {
    }

    public BorrowerProfile(String fullName, String panNumber, BigDecimal monthlyIncome, String employmentType, User user) {
        this.fullName = fullName;
        this.panNumber = panNumber;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}