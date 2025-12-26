package com.example.Al.Baraka.enums;

public enum OperationStatus {
    PENDING,  // En attente de validation
    APPROVED,
    REJECTED,
    COMPLETED     // Complétée (pour montants <= 10000)
}
