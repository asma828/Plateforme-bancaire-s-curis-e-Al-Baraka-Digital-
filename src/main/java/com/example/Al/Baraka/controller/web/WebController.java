package com.example.Al.Baraka.controller.web;

import com.example.Al.Baraka.dto.response.AccountResponse;
import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.model.AIValidation;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.repository.AIValidationRepository;
import com.example.Al.Baraka.repository.OperationRepository;
import com.example.Al.Baraka.security.CustomUserDetails;
import com.example.Al.Baraka.service.AccountService;
import com.example.Al.Baraka.service.AgentService;
import com.example.Al.Baraka.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final AccountService accountService;
    private final OperationService operationService;
    private final AgentService agentService;
    private final AIValidationRepository aiValidationRepository;
    private final OperationRepository operationRepository;

    /**
     * Page d'accueil - Redirection selon le rôle
     */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        return switch (userDetails.getRole()) {
            case "CLIENT" -> "redirect:/web/client/dashboard";
            case "AGENT_BANCAIRE" -> "redirect:/web/agent/dashboard";
            case "ADMIN" -> "redirect:/web/admin/dashboard";
            default -> "redirect:/login";
        };
    }

    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès");
        }
        return "login";
    }

    /**
     * Dashboard Client
     */
    @GetMapping("/web/client/dashboard")
    public String clientDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        String email = userDetails.getUsername();

        // Récupérer les informations du compte
        AccountResponse account = accountService.getAccountByEmail(email);
        model.addAttribute("account", account);

        // Récupérer les opérations
        List<OperationResponse> operations = operationService.getOperationsByEmail(email);
        model.addAttribute("operations", operations);

        // Statistiques
        long pendingCount = operations.stream()
                .filter(op -> op.getStatus().name().equals("PENDING"))
                .count();
        long completedCount = operations.stream()
                .filter(op -> op.getStatus().name().equals("COMPLETED") || op.getStatus().name().equals("APPROVED"))
                .count();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalOperations", operations.size());

        return "client/dashboard";
    }

    /**
     * Page de création d'opération
     */
    @GetMapping("/web/client/operations/new")
    public String newOperation(Model model) {
        return "client/new-operation";
    }

    /**
     * Détails d'une opération
     */
    @GetMapping("/web/client/operations/{id}")
    public String operationDetails(@PathVariable Long id, Model model) {
        OperationResponse operation = operationService.getOperationById(id);
        model.addAttribute("operation", operation);

        // Récupérer l'analyse IA si elle existe
        Optional<Operation> op = operationRepository.findById(id);
        if (op.isPresent()) {
            Optional<AIValidation> aiValidation = aiValidationRepository.findByOperation(op.get());
            aiValidation.ifPresent(validation -> {
                model.addAttribute("aiValidation", validation);
                model.addAttribute("hasAIValidation", true);
            });
        }

        return "client/operation-details";
    }

    /**
     * Dashboard Agent
     */
    @GetMapping("/web/agent/dashboard")
    public String agentDashboard(Model model) {
        List<OperationResponse> pendingOperations = agentService.getPendingOperations();
        model.addAttribute("operations", pendingOperations);
        model.addAttribute("pendingCount", pendingOperations.size());

        return "agent/dashboard";
    }

    /**
     * Détails d'une opération pour l'agent
     */
    @GetMapping("/web/agent/operations/{id}")
    public String agentOperationDetails(@PathVariable Long id, Model model) {
        OperationResponse operation = operationService.getOperationById(id);
        model.addAttribute("operation", operation);

        // Récupérer l'analyse IA
        Optional<Operation> op = operationRepository.findById(id);
        if (op.isPresent()) {
            Optional<AIValidation> aiValidation = aiValidationRepository.findByOperation(op.get());
            aiValidation.ifPresent(validation -> {
                model.addAttribute("aiValidation", validation);
                model.addAttribute("hasAIValidation", true);
            });
        }

        return "agent/operation-details";
    }

    /**
     * Dashboard Admin
     */
    @GetMapping("/web/admin/dashboard")
    public String adminDashboard(Model model) {
        // Statistiques globales
        List<AIValidation> allValidations = aiValidationRepository.findAll();

        long totalValidations = allValidations.size();
        long approvedCount = allValidations.stream()
                .filter(v -> v.getDecision().name().equals("APPROVE"))
                .count();
        long rejectedCount = allValidations.stream()
                .filter(v -> v.getDecision().name().equals("REJECT"))
                .count();
        long reviewCount = allValidations.stream()
                .filter(v -> v.getDecision().name().equals("NEED_HUMAN_REVIEW"))
                .count();

        double avgConfidence = allValidations.stream()
                .mapToDouble(AIValidation::getConfidenceScore)
                .average()
                .orElse(0.0);

        model.addAttribute("totalValidations", totalValidations);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("avgConfidence", (int)(avgConfidence * 100));

        return "admin/dashboard";
    }

    /**
     * Dashboard générique (fallback)
     */
    @GetMapping("/web/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return home(userDetails);
    }
}