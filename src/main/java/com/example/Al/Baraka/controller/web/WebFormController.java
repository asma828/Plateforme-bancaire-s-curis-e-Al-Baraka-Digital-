package com.example.Al.Baraka.controller.web;

import com.example.Al.Baraka.dto.request.OperationRequest;
import com.example.Al.Baraka.dto.response.OperationResponse;
import com.example.Al.Baraka.model.Document;
import com.example.Al.Baraka.security.CustomUserDetails;
import com.example.Al.Baraka.service.AgentService;
import com.example.Al.Baraka.service.DocumentService;
import com.example.Al.Baraka.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebFormController {

    private final OperationService operationService;
    private final DocumentService documentService;
    private final AgentService agentService;

    /**
     * Traiter le formulaire de création d'opération
     */
    @PostMapping("/client/operations/create")
    public String createOperation(@ModelAttribute OperationRequest request,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            OperationResponse operation = operationService.createOperation(email, request);

            redirectAttributes.addFlashAttribute("message",
                    "Opération #" + operation.getId() + " créée avec succès !");

            // Si document requis, rediriger vers la page de détails avec alerte upload
            if (operation.getRequiresDocument()) {
                return "redirect:/web/client/operations/" + operation.getId() + "?uploadRequired=true";
            }

            return "redirect:/web/client/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/client/operations/new";
        }
    }

    /**
     * Traiter l'upload de document via formulaire web
     */
    @PostMapping("/client/operations/{id}/upload")
    public String uploadDocument(@PathVariable Long id,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            Document document = documentService.uploadDocument(id, file);
            redirectAttributes.addFlashAttribute("message",
                    "Document uploadé avec succès ! L'analyse IA est en cours...");
            return "redirect:/web/client/operations/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'upload : " + e.getMessage());
            return "redirect:/web/client/operations/" + id;
        }
    }

    /**
     * Approuver une opération (Agent)
     */
    @PostMapping("/agent/operations/{id}/approve")
    public String approveOperation(@PathVariable Long id,
                                   @RequestParam(required = false) String comment,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            String agentEmail = userDetails.getUsername();
            agentService.approveOperation(id, agentEmail);
            redirectAttributes.addFlashAttribute("message",
                    "Opération #" + id + " approuvée avec succès !");
            return "redirect:/web/agent/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/agent/operations/" + id;
        }
    }

    /**
     * Rejeter une opération (Agent)
     */
    @PostMapping("/agent/operations/{id}/reject")
    public String rejectOperation(@PathVariable Long id,
                                  @RequestParam(required = false) String comment,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            String agentEmail = userDetails.getUsername();
            agentService.rejectOperation(id, agentEmail);
            redirectAttributes.addFlashAttribute("message",
                    "Opération #" + id + " rejetée.");
            return "redirect:/web/agent/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/agent/operations/" + id;
        }
    }
}