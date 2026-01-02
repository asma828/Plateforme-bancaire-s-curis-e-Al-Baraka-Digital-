package com.example.Al.Baraka.repository;

import com.example.Al.Baraka.model.AIValidation;
import com.example.Al.Baraka.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIValidationRepository extends JpaRepository<AIValidation, Long> {

    Optional<AIValidation> findByOperation(Operation operation);

    boolean existsByOperation(Operation operation);
}