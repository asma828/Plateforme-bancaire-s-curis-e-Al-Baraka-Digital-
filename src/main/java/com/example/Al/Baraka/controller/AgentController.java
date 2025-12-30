package com.example.Al.Baraka.controller;

import com.example.Al.Baraka.dto.response.ApiResponse;
import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.dto.request.ValidationRequest;
import com.example.Al.Baraka.service.AgentService;
import com.example.Al.Baraka.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final DocumentService documentService;

    /**
     * Endpoint protégé par OAuth2 - Scope: operations.read
     * L'agent doit présenter un access token OAuth2 valide depuis Keycloak
     */
    @GetMapping("/operations/pending")
    public ResponseEntity<ApiResponse<List<OperationResponse>>> getPendingOperations() {
        try {
            List<OperationResponse> operations = agentService.getPendingOperations();
            return ResponseEntity.ok(ApiResponse.success(
                    "Pending operations retrieved successfully",
                    operations
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Approuver une opération - Protégé par JWT
     */
    @PutMapping("/operations/{id}/approve")
    public ResponseEntity<ApiResponse<OperationResponse>> approveOperation(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationRequest request,
            Authentication authentication) {
        try {
            String agentEmail = authentication.getName();
            OperationResponse operation = agentService.approveOperation(id, agentEmail);
            return ResponseEntity.ok(ApiResponse.success(
                    "Operation approved successfully",
                    operation
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Rejeter une opération - Protégé par JWT
     */
    @PutMapping("/operations/{id}/reject")
    public ResponseEntity<ApiResponse<OperationResponse>> rejectOperation(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationRequest request,
            Authentication authentication) {
        try {
            String agentEmail = authentication.getName();
            OperationResponse operation = agentService.rejectOperation(id, agentEmail);
            return ResponseEntity.ok(ApiResponse.success(
                    "Operation rejected successfully",
                    operation
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Consulter le document d'une opération - Protégé par JWT
     */
    @GetMapping("/operations/{operationId}/document")
    public ResponseEntity<?> getOperationDocument(@PathVariable Long operationId) {
        try {
            var document = documentService.getDocumentByOperationId(operationId);
            byte[] content = documentService.getDocumentContent(document.getId());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header("Content-Disposition", "inline; filename=\"" + document.getFileName() + "\"")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}