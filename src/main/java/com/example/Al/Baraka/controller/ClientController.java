package com.example.Al.Baraka.controller;


import com.example.Al.Baraka.dto.response.AccountResponse;
import com.example.Al.Baraka.dto.response.ApiResponse;
import com.example.Al.Baraka.dto.request.OperationRequest;
import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.model.Document;
import com.example.Al.Baraka.security.CustomUserDetails;
import com.example.Al.Baraka.service.AccountService;
import com.example.Al.Baraka.service.DocumentService;
import com.example.Al.Baraka.service.OperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final OperationService operationService;
    private final AccountService accountService;
    private final DocumentService documentService;

    // Récupérer les informations du compte du client connecté
    @GetMapping("/account")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(Authentication authentication) {
        try {
            String email = authentication.getName();
            AccountResponse account = accountService.getAccountByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Créer une nouvelle opération
    @PostMapping("/operations")
    public ResponseEntity<ApiResponse<OperationResponse>> createOperation(
            @Valid @RequestBody OperationRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            OperationResponse operation = operationService.createOperation(email, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Operation created successfully", operation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Lister toutes les opérations du client
    @GetMapping("/operations")
    public ResponseEntity<ApiResponse<List<OperationResponse>>> getOperations(Authentication authentication) {
        try {
            String email = authentication.getName();

            List<OperationResponse> operations = operationService.getOperationsByEmail(email);
            return ResponseEntity.ok(ApiResponse.success("Operations retrieved successfully", operations));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Récupérer une opération par ID
    @GetMapping("/operations/{id}")
    public ResponseEntity<ApiResponse<OperationResponse>> getOperation(@PathVariable Long id) {
        try {
            OperationResponse operation = operationService.getOperationById(id);
            return ResponseEntity.ok(ApiResponse.success("Operation retrieved successfully", operation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Upload un document justificatif pour une opération
    @PostMapping("/operations/{id}/document")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Document document = documentService.uploadDocument(id, file);
            return ResponseEntity.ok(ApiResponse.success(
                    "Document uploaded successfully",
                    "Document ID: " + document.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Télécharger un document
    @GetMapping("/documents/{id}")
    public ResponseEntity<?> downloadDocument(@PathVariable Long id) {
        try {
            byte[] content = documentService.getDocumentContent(id);
            Document document = documentService.getDocumentByOperationId(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header("Content-Disposition", "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
