package com.example.Al.Baraka.service;

import com.example.Al.Baraka.enums.AIDecision;
import com.example.Al.Baraka.model.AIValidation;
import com.example.Al.Baraka.model.Document;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.repository.AIValidationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Service d'analyse intelligente des documents avec Spring AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIDocumentAnalysisService {

    private final ChatModel chatModel;
    private final AIValidationRepository aiValidationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Analyse un document et retourne une décision IA
     */
    @Transactional
    public AIValidation analyzeDocument(Operation operation, Document document) {
        log.info("🤖 Starting AI analysis for operation #{}", operation.getId());
        long startTime = System.currentTimeMillis();

        try {
            // 1. Extraire le texte du document
            String extractedText = extractTextFromDocument(document);
            log.info("📄 Extracted {} characters from document", extractedText.length());

            // 2. Construire le prompt pour l'IA
            String prompt = buildAnalysisPrompt(operation, extractedText);

            // 3. Appeler l'IA pour analyse
            ChatResponse response = chatModel.call(new Prompt(prompt));

            // Correction: Utiliser getText() au lieu de getContent()
            String aiResponse = response.getResult().getOutput().getText();

            log.info("🧠 AI Response: {}", aiResponse);

            // 4. Parser la réponse JSON de l'IA
            AIDecisionResult result = parseAIResponse(aiResponse);

            // 5. Créer et sauvegarder la validation IA
            long processingTime = System.currentTimeMillis() - startTime;

            AIValidation validation = AIValidation.builder()
                    .operation(operation)
                    .decision(result.getDecision())
                    .confidenceScore(result.getConfidenceScore())
                    .reasoning(result.getReasoning())
                    .extractedText(extractedText.length() > 2000 ? extractedText.substring(0, 2000) : extractedText)
                    .processingTimeMs(processingTime)
                    .build();

            validation = aiValidationRepository.save(validation);

            log.info("✅ AI analysis completed in {}ms - Decision: {} (confidence: {}%)",
                    processingTime, result.getDecision(), (int) (result.getConfidenceScore() * 100));

            return validation;

        } catch (Exception e) {
            log.error("❌ Error during AI analysis", e);

            // En cas d'erreur, créer une validation avec décision NEED_HUMAN_REVIEW
            AIValidation fallbackValidation = AIValidation.builder()
                    .operation(operation)
                    .decision(AIDecision.NEED_HUMAN_REVIEW)
                    .confidenceScore(0.0)
                    .reasoning("Error during AI analysis: " + e.getMessage())
                    .extractedText("Error extracting text")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();

            return aiValidationRepository.save(fallbackValidation);
        }
    }

    /**
     * Extrait le texte d'un document (PDF ou image)
     */
    private String extractTextFromDocument(Document document) throws IOException {
        String filePath = document.getStoragePath();
        String fileType = document.getFileType().toLowerCase();

        if (fileType.contains("pdf")) {
            return extractTextFromPDF(filePath);
        } else if (fileType.contains("image") || fileType.contains("jpg") || fileType.contains("png")) {
            // Pour les images, on retourne les métadonnées
            // Note: Pour une vraie OCR, il faudrait intégrer Tesseract ou un service cloud
            return extractMetadataFromImage(filePath);
        } else {
            throw new IOException("Unsupported file type: " + fileType);
        }
    }

    /**
     * Extrait le texte d'un PDF avec PDFBox 3.x
     */
    private String extractTextFromPDF(String filePath) throws IOException {
        // Correction: PDFBox 3.x utilise Loader.loadPDF() au lieu de PDDocument.load()
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extrait les métadonnées d'une image
     * Note: Pour une vraie OCR, intégrer Tesseract
     */
    private String extractMetadataFromImage(String filePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(filePath));
        return String.format("Image document: %dx%d pixels. Note: OCR not implemented, manual review required.",
                image.getWidth(), image.getHeight());
    }

    /**
     * Construit le prompt pour l'analyse IA
     */
    private String buildAnalysisPrompt(Operation operation, String extractedText) {
        return String.format("""
            You are an AI banking document validator. Analyze the following document for a banking operation.
            
            OPERATION DETAILS:
            - Type: %s
            - Amount: %.2f DH
            - Account: %s
            
            DOCUMENT CONTENT:
            %s
            
            VALIDATION RULES:
            1. The document must be clear and readable
            2. The document should justify the operation amount
            3. For deposits: Check for proof of income, bank statement, or transfer proof
            4. For withdrawals: Check for valid justification
            5. For transfers: Verify beneficiary information
            
            RESPOND ONLY with a valid JSON object in this exact format:
            {
                "decision": "APPROVE|REJECT|NEED_HUMAN_REVIEW",
                "confidenceScore": 0.85,
                "reasoning": "Your explanation here"
            }
            
            Decision criteria:
            - APPROVE: Document is clear, complete, and validates the operation (confidence > 0.80)
            - REJECT: Document is clearly invalid or fraudulent (confidence > 0.80)
            - NEED_HUMAN_REVIEW: Uncertain case or requires human judgment (confidence < 0.80)
            """,
                operation.getType(),
                operation.getAmount(),
                operation.getAccountSource().getAccountNumber(),
                extractedText.length() > 1500 ? extractedText.substring(0, 1500) + "..." : extractedText
        );
    }

    /**
     * Parse la réponse JSON de l'IA
     */
    private AIDecisionResult parseAIResponse(String aiResponse) {
        try {
            // Nettoyer la réponse (enlever les markdown code blocks si présents)
            String cleanedResponse = aiResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

            String decisionStr = jsonNode.get("decision").asText();
            double confidenceScore = jsonNode.get("confidenceScore").asDouble();
            String reasoning = jsonNode.get("reasoning").asText();

            AIDecision decision = AIDecision.valueOf(decisionStr);

            return new AIDecisionResult(decision, confidenceScore, reasoning);

        } catch (Exception e) {
            log.error("Error parsing AI response: {}", aiResponse, e);
            // Fallback en cas d'erreur de parsing
            return new AIDecisionResult(
                    AIDecision.NEED_HUMAN_REVIEW,
                    0.0,
                    "Error parsing AI response"
            );
        }
    }

    /**
     * Classe interne pour le résultat de l'analyse IA
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AIDecisionResult {
        private AIDecision decision;
        private Double confidenceScore;
        private String reasoning;
    }
}