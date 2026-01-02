package com.example.Al.Baraka.service;

import com.example.Al.Baraka.enums.AIDecision;
import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.model.AIValidation;
import com.example.Al.Baraka.model.Document;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.repository.DocumentRepository;
import com.example.Al.Baraka.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;
    private final AIDocumentAnalysisService aiAnalysisService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload un document et déclenche l'analyse IA automatique
     */
    @Transactional
    public Document uploadDocument(Long operationId, MultipartFile file) throws IOException {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        // Vérifier si l'opération a déjà un document
        if (documentRepository.existsByOperation(operation)) {
            throw new RuntimeException("Operation already has a document");
        }

        // Vérifier que l'opération est en attente
        if (operation.getStatus() != OperationStatus.PENDING) {
            throw new RuntimeException("Operation is not in pending status");
        }

        // Valider le fichier
        validateFile(file);

        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(newFilename);

        // Copier le fichier
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Créer l'entité Document
        Document document = Document.builder()
                .fileName(originalFilename)
                .fileType(file.getContentType())
                .storagePath(filePath.toString())
                .operation(operation)
                .build();

        document = documentRepository.save(document);
        log.info("📎 Document uploaded for operation #{}: {}", operationId, originalFilename);

        // 🤖 Déclencher l'analyse IA automatique
        try {
            AIValidation aiValidation = aiAnalysisService.analyzeDocument(operation, document);

            // Traiter la décision IA
            processAIDecision(operation, aiValidation);

        } catch (Exception e) {
            log.error("❌ Error during AI analysis, operation will require manual review", e);
            // L'opération reste en PENDING pour revue manuelle
        }

        return document;
    }

    /**
     * Traite la décision de l'IA et met à jour l'opération en conséquence
     */
    @Transactional
    public void processAIDecision(Operation operation, AIValidation aiValidation) {
        AIDecision decision = aiValidation.getDecision();

        log.info("🎯 Processing AI decision: {} for operation #{}", decision, operation.getId());

        switch (decision) {
            case APPROVE:
                // Auto-approuver l'opération
                if (aiValidation.getConfidenceScore() >= 0.85) {
                    executeOperation(operation);
                    operation.setStatus(OperationStatus.APPROVED);
                    operation.setExecutedAt(LocalDateTime.now());
                    operationRepository.save(operation);
                    log.info("✅ Operation #{} auto-approved by AI", operation.getId());
                } else {
                    // Confiance insuffisante, nécessite revue humaine
                    log.info("⚠️ Operation #{} requires human review (low confidence)", operation.getId());
                }
                break;

            case REJECT:
                // Auto-rejeter l'opération
                if (aiValidation.getConfidenceScore() >= 0.85) {
                    operation.setStatus(OperationStatus.REJECTED);
                    operation.setValidatedAt(LocalDateTime.now());
                    operationRepository.save(operation);
                    log.info("❌ Operation #{} auto-rejected by AI", operation.getId());
                } else {
                    // Confiance insuffisante, nécessite revue humaine
                    log.info("⚠️ Operation #{} requires human review (low confidence)", operation.getId());
                }
                break;

            case NEED_HUMAN_REVIEW:
                // Laisser en PENDING pour validation humaine
                log.info("👤 Operation #{} requires human review", operation.getId());
                break;
        }
    }

    /**
     * Exécute l'opération bancaire (mise à jour des soldes)
     */
    private void executeOperation(Operation operation) {
        switch (operation.getType()) {
            case DEPOSIT:
                operation.getAccountSource().setBalance(
                        operation.getAccountSource().getBalance().add(operation.getAmount())
                );
                break;

            case WITHDRAWAL:
                operation.getAccountSource().setBalance(
                        operation.getAccountSource().getBalance().subtract(operation.getAmount())
                );
                break;

            case TRANSFER:
                operation.getAccountSource().setBalance(
                        operation.getAccountSource().getBalance().subtract(operation.getAmount())
                );
                operation.getAccountDestination().setBalance(
                        operation.getAccountDestination().getBalance().add(operation.getAmount())
                );
                break;
        }
    }

    public Document getDocumentByOperationId(Long operationId) {
        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        return documentRepository.findByOperation(operation)
                .orElseThrow(() -> new RuntimeException("Document not found for this operation"));
    }

    public byte[] getDocumentContent(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Path filePath = Paths.get(document.getStoragePath());
        return Files.readAllBytes(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum limit of 5MB");
        }

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("File type not allowed. Only PDF, JPG, JPEG, PNG are accepted");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}