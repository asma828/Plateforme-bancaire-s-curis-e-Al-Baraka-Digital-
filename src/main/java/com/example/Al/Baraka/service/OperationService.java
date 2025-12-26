package com.example.Al.Baraka.service;

import com.example.Al.Baraka.dto.request.OperationRequest;
import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.model.User;
import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.enums.OperationType;
import com.example.Al.Baraka.repository.AccountRepository;
import com.example.Al.Baraka.repository.OperationRepository;
import com.example.Al.Baraka.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationService {

    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private static final BigDecimal VALIDATION_THRESHOLD = new BigDecimal("10000");

    @Transactional
    public OperationResponse createOperation(String email, OperationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account sourceAccount = accountRepository.findByOwner(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Validation selon le type d'opération
        switch (request.getType()) {
            case DEPOSIT:
                return processDeposit(sourceAccount, request);
            case WITHDRAWAL:
                return processWithdrawal(sourceAccount, request);
            case TRANSFER:
                return processTransfer(sourceAccount, request);
            default:
                throw new RuntimeException("Invalid operation type");
        }
    }

    private OperationResponse processDeposit(Account account, OperationRequest request) {
        Operation operation = Operation.builder()
                .type(OperationType.DEPOSIT)
                .amount(request.getAmount())
                .accountSource(account)
                .build();

        boolean requiresValidation = request.getAmount().compareTo(VALIDATION_THRESHOLD) > 0;

        if (requiresValidation) {
            operation.setStatus(OperationStatus.PENDING);
            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Deposit created. Document required for validation.");
        } else {
            // Auto-validation pour montants <= 10000
            operation.setStatus(OperationStatus.COMPLETED);
            operation.setExecutedAt(LocalDateTime.now());

            // Mettre à jour le solde
            account.setBalance(account.getBalance().add(request.getAmount()));
            accountRepository.save(account);

            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Deposit completed successfully.");
        }
    }

    private OperationResponse processWithdrawal(Account account, OperationRequest request) {
        // Vérifier le solde suffisant
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Operation operation = Operation.builder()
                .type(OperationType.WITHDRAWAL)
                .amount(request.getAmount())
                .accountSource(account)
                .build();

        boolean requiresValidation = request.getAmount().compareTo(VALIDATION_THRESHOLD) > 0;

        if (requiresValidation) {
            operation.setStatus(OperationStatus.PENDING);
            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Withdrawal created. Document required for validation.");
        } else {
            // Auto-validation pour montants <= 10000
            operation.setStatus(OperationStatus.COMPLETED);
            operation.setExecutedAt(LocalDateTime.now());

            // Mettre à jour le solde
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            accountRepository.save(account);

            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Withdrawal completed successfully.");
        }
    }

    private OperationResponse processTransfer(Account sourceAccount, OperationRequest request) {
        // Vérifier que le compte destination est fourni
        if (request.getDestinationAccountNumber() == null || request.getDestinationAccountNumber().isEmpty()) {
            throw new RuntimeException("Destination account number is required for transfers");
        }

        // Vérifier le solde suffisant
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Récupérer le compte destination
        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        // Vérifier qu'on ne transfère pas vers le même compte
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        Operation operation = Operation.builder()
                .type(OperationType.TRANSFER)
                .amount(request.getAmount())
                .accountSource(sourceAccount)
                .accountDestination(destinationAccount)
                .build();

        boolean requiresValidation = request.getAmount().compareTo(VALIDATION_THRESHOLD) > 0;

        if (requiresValidation) {
            operation.setStatus(OperationStatus.PENDING);
            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Transfer created. Document required for validation.");
        } else {
            // Auto-validation pour montants <= 10000
            operation.setStatus(OperationStatus.COMPLETED);
            operation.setExecutedAt(LocalDateTime.now());

            // Mettre à jour les soldes
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            operation = operationRepository.save(operation);

            return mapToResponse(operation, "Transfer completed successfully.");
        }
    }

    public List<OperationResponse> getOperationsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findByOwner(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return operationRepository.findByAccountOrderByCreatedAtDesc(account).stream()
                .map(op -> mapToResponse(op, null))
                .collect(Collectors.toList());
    }


    public OperationResponse getOperationById(Long operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        return mapToResponse(operation, null);
    }

    private OperationResponse mapToResponse(Operation operation, String message) {
        boolean requiresDocument = operation.getAmount().compareTo(VALIDATION_THRESHOLD) > 0;

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
                .message(message)
                .requiresDocument(requiresDocument)
                .hasDocument(operation.getDocument() != null)
                .build();
    }
}
