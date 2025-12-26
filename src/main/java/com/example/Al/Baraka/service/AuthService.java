package com.example.Al.Baraka.service;


import com.example.Al.Baraka.dto.response.AuthResponse;
import com.example.Al.Baraka.dto.request.LoginRequest;
import com.example.Al.Baraka.dto.request.RegisterRequest;
import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.User;
import com.example.Al.Baraka.enums.Role;
import com.example.Al.Baraka.repository.AccountRepository;
import com.example.Al.Baraka.repository.UserRepository;
import com.example.Al.Baraka.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.CLIENT)
                .active(true)
                .build();

        user = userRepository.save(user);

        // Créer un compte bancaire automatiquement pour le client
        String accountNumber = generateAccountNumber();
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .owner(user)
                .build();

        accountRepository.save(account);

        // Générer le token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accountNumber(accountNumber)
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Authentifier l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Récupérer l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vérifier si le compte est actif
        if (!user.getActive()) {
            throw new RuntimeException("Account is disabled");
        }

        // Récupérer le premier compte de l'utilisateur
        Account account = accountRepository.findByOwner(user).stream()
                .findFirst()
                .orElse(null);

        // Générer le token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accountNumber(account != null ? account.getAccountNumber() : null)
                .message("Login successful")
                .build();
    }

    private String generateAccountNumber() {
        // Générer un numéro de compte unique (format: AB-XXXXXX)
        String accountNumber;
        do {
            accountNumber = "AB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
