package com.example.Al.Baraka.model;

import com.example.Al.Baraka.enums.AIDecision;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_validations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "operation") // Exclure la relation
@EntityListeners(AuditingEntityListener.class)
public class AIValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIDecision decision;

    @Column(nullable = false)
    private Double confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
}