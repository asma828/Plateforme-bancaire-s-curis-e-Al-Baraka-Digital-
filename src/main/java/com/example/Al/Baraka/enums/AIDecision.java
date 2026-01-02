package com.example.Al.Baraka.enums;

/**
 * Décisions possibles de l'analyse IA des documents
 */
public enum AIDecision {
    APPROVE,              // Document valide, approuver automatiquement
    REJECT,               // Document invalide, rejeter automatiquement
    NEED_HUMAN_REVIEW     // Cas ambigu, nécessite validation humaine
}