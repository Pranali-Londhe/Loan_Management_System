```markdown
# Loan Management System

A full-stack web application built with **Spring Boot**, **Thymeleaf**, **Spring Security**, **Spring Data JPA**, and **MySQL** to manage end-to-end loan applications, EMI calculations, admin approvals, disbursements, and installment repayments.

---

## Features

### Borrower
- Register and authenticate securely (`BORROWER` role).
- Calculate monthly EMI, total interest, and total payable amount before applying.
- Submit loan applications with validation on requested amount and tenure.
- Track loan statuses (`PENDING`, `APPROVED`, `REJECTED`, `ACTIVE`, `CLOSED`).
- View itemized repayment schedules and pay monthly installments.

### Admin
- Authenticate with administrative access (`ADMIN` role).
- Monitor dashboard metrics (total amount lent, pending approvals, and active loans).
- Review pending loan applications and approve, reject, or disburse funds.

---

## Tech Stack

- **Backend:** Java, Spring Boot (Spring Web, Spring Security, Spring Data JPA, Bean Validation)
- **Frontend:** Thymeleaf, HTML5, CSS3, Vanilla JavaScript
- **Database:** MySQL
- **Build & Testing:** Maven, JUnit

---

## Project Structure

```text
loan-management-system/
├── pom.xml                                        # Maven dependencies (Spring Web, Thymeleaf, JPA, MySQL, Validation)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/loanmgmt/
    │   │       ├── LoanManagementApplication.java   # Main entry point that starts the server
    │   │       ├── config/
    │   │       │   └── SecurityConfig.java          # Login rules & role access (BORROWER vs ADMIN)
    │   │       ├── controller/
    │   │       │   ├── AuthController.java          # Handles login & registration page requests
    │   │       │   ├── LoanController.java          # Borrower loan apply, list, details & repayment routes
    │   │       │   └── AdminLoanController.java     # Admin review, approve, reject & disburse routes
    │   │       ├── dto/
    │   │       │   └── LoanApplicationRequest.java  # Form input object + validation rules (amount, tenure)
    │   │       ├── entity/
    │   │       │   ├── User.java                    # Maps to `users` table in MySQL
    │   │       │   ├── Loan.java                    # Maps to `loans` table in MySQL
    │   │       │   └── RepaymentSchedule.java       # Maps to `repayment_schedules` table in MySQL
    │   │       ├── enums/
    │   │       │   ├── Role.java                    # BORROWER, ADMIN
    │   │       │   └── LoanStatus.java              # PENDING, APPROVED, REJECTED, ACTIVE, CLOSED
    │   │       ├── exception/
    │   │       │   └── GlobalExceptionHandler.java  # Catches errors cleanly & shows friendly error pages
    │   │       ├── repository/
    │   │       │   ├── UserRepository.java          # Database queries for users
    │   │       │   ├── LoanRepository.java          # Database queries for loans
    │   │       │   └── RepaymentRepository.java     # Database queries for installments
    │   │       └── service/
    │   │           ├── EmiCalculatorService.java    # Pure math: calculates EMI, interest & total payable
    │   │           ├── LoanService.java             # Business rules: eligibility, apply, approve, repay
    │   │           └── CustomUserDetailsService.java# Connects MySQL users to login authentication
    │   └── resources/
    │       ├── application.properties               # DB URL, username, password, server port, Thymeleaf settings
    │       ├── static/                              # Publicly served CSS, JS, images (no server processing)
    │       │   ├── css/
    │       │   │   └── style.css                    # Layout, cards, status badges, table styling
    │       │   └── js/
    │       │       └── loan-calculator.js           # Optional live EMI preview in the browser
    │       └── templates/                           # Thymeleaf HTML files (rendered by Java before sending)
    │           ├── fragments/
    │           │   ├── navbar.html                  # Shared top navigation bar across pages
    │           │   └── footer.html                  # Shared footer across pages
    │           ├── auth/
    │           │   ├── login.html                   # Login screen
    │           │   └── register.html                # Borrower sign-up screen
    │           ├── borrower/
    │           │   ├── dashboard.html               # Borrower summary cards & quick links
    │           │   ├── apply-loan.html              # Form to submit a new loan request
    │           │   ├── my-loans.html                # Table of all loans belonging to logged-in user
    │           │   └── loan-details.html            # Repayment schedule table + "Pay Installment" button
    │           ├── admin/
    │           │   ├── dashboard.html               # Total lent, pending approvals, active loans counts
    │           │   └── pending-loans.html           # Queue with Approve / Reject / Disburse actions
    │           └── error/
    │               └── error.html                   # Friendly fallback page when something goes wrong
    └── test/
        └── java/
            └── com/example/loanmgmt/
                └── EmiCalculatorServiceTest.java    # Unit tests for EMI & balance calculations

```

---

## Getting Started

### Prerequisites

* **JDK 17+**
* **Maven 3.8+**
* **MySQL 8.0+**

### 1. Clone the Repository

```bash
git clone [https://github.com/Pranali-Londhe/Loan_Management_System.git](https://github.com/Pranali-Londhe/Loan_Management_System.git)
cd Loan_Management_System

```

### 2. Configure MySQL

Create a database in MySQL:

```sql
CREATE DATABASE loan_management_db;

```

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loan_management_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update

```

### 3. Run the Application

```bash
./mvnw spring-boot:run

```

*(On Windows PowerShell, use `.\mvnw.cmd spring-boot:run`.)*

Open `http://localhost:8080` in your browser.

### 4. Run Tests

```bash
./mvnw test

```

---

## Team Workflow

1. Pull the latest `main` branch before starting work:
```bash
git checkout main
git pull origin main

```


2. Create a feature branch for your module:
```bash
git checkout -b feature/<module-name>

```


3. Commit and push your changes, then open a Pull Request into `main`.

```

```
