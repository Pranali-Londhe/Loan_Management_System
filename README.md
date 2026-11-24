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
