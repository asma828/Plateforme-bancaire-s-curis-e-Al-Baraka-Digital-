package com.example.Al.Baraka.model;


import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime validatedAt;

    private LocalDateTime executedAt;

    @ManyToOne
    @JoinColumn(name = "account_source_id", nullable = false)
    private Account accountSource;

    @ManyToOne
    @JoinColumn(name = "account_destination_id")
    private Account accountDestination;

    @OneToOne(mappedBy = "operation", cascade = CascadeType.ALL)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "validated_by")
    private User validatedBy;
}
