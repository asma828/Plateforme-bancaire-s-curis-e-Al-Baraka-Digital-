package com.example.Al.Baraka.service;


import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.model.User;
import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.repository.AccountRepository;
import com.example.Al.Baraka.repository.OperationRepository;
import com.example.Al.Baraka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;


    @Transactional
    public OperationResponse approveOperation(Long operationId, String agentEmail) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Operation is not pending");
        }

        // Vérifier qu'il y a un document pour les opérations > 10000
        if (operation.getDocument() == null) {
            throw new RuntimeException("Document is required for this operation");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Exécuter l'opération selon le type
        switch (operation.getType()) {
            case DEPOSIT:
                executeDeposit(operation);
                break;
            case WITHDRAWAL:
                executeWithdrawal(operation);
                break;
            case TRANSFER:
                executeTransfer(operation);
                break;
        }

        operation.setStatus(OperationStatus.APPROVED);
        operation.setValidatedAt(LocalDateTime.now());
        operation.setExecutedAt(LocalDateTime.now());
        operation.setValidatedBy(agent);

        operation = operationRepository.save(operation);

        return mapToResponse(operation);
    }

    @Transactional
    public OperationResponse rejectOperation(Long operationId, String agentEmail) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Operation is not pending");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        operation.setStatus(OperationStatus.REJECTED);
        operation.setValidatedAt(LocalDateTime.now());
        operation.setValidatedBy(agent);

        operation = operationRepository.save(operation);

        return mapToResponse(operation);
    }

    private void executeDeposit(Operation operation) {
        Account account = operation.getAccountSource();
        account.setBalance(account.getBalance().add(operation.getAmount()));
        accountRepository.save(account);
    }

    private void executeWithdrawal(Operation operation) {
        Account account = operation.getAccountSource();

        if (account.getBalance().compareTo(operation.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(operation.getAmount()));
        accountRepository.save(account);
    }

    private void executeTransfer(Operation operation) {
        Account sourceAccount = operation.getAccountSource();
        Account destinationAccount = operation.getAccountDestination();

        if (sourceAccount.getBalance().compareTo(operation.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(operation.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(operation.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
    }

    private OperationResponse mapToResponse(Operation operation) {
        return OperationResponse.builder()
                .id(operation.getId())
                .type(operation.getType())
                .amount(operation.getAmount())
                .status(operation.getStatus())
                .createdAt(operation.getCreatedAt())
                .validatedAt(operation.getValidatedAt())
                .executedAt(operation.getExecutedAt())
                .sourceAccountNumber(operation.getAccountSource().getAccountNumber())
                .destinationAccountNumber(
                        operation.getAccountDestination() != null
                                ? operation.getAccountDestination().getAccountNumber()
                                : null
                )
                .hasDocument(operation.getDocument() != null)
                .build();
    }
}