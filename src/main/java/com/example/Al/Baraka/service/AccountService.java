package com.example.Al.Baraka.service;

import com.example.Al.Baraka.dto.response.AccountResponse;
import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.User;
import com.example.Al.Baraka.repository.AccountRepository;
import com.example.Al.Baraka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return mapToResponse(account);
    }

    public List<AccountResponse> getAccountsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accountRepository.findByOwner(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByOwner(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found for this user"));

        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .ownerEmail(account.getOwner().getEmail())
                .ownerFullName(account.getOwner().getFullName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
