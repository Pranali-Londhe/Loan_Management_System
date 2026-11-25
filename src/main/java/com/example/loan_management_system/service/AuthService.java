package com.example.loan_management_system.service;

import com.example.loan_management_system.dto.AuthResponse;
import com.example.loan_management_system.dto.LoginRequest;
import com.example.loan_management_system.dto.RegisterRequest;
import com.example.loan_management_system.entity.BorrowerProfile;
import com.example.loan_management_system.entity.User;
import com.example.loan_management_system.enums.Role;
import com.example.loan_management_system.repository.UserRepository;
import com.example.loan_management_system.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse registerBorrower(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.BORROWER);

        BorrowerProfile profile = new BorrowerProfile(
                request.getFullName().trim(),
                request.getPanNumber() != null ? request.getPanNumber().trim().toUpperCase() : null,
                request.getMonthlyIncome(),
                request.getEmploymentType().trim(),
                user
        );

        user.setBorrowerProfile(profile);
        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getEmail(),
                saved.getRole().name(),
                "Borrower registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Login successful"
        );
    }
}