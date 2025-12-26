package com.example.Al.Baraka.repository;



import com.example.Al.Baraka.model.Document;
import com.example.Al.Baraka.model.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByOperation(Operation operation);

    boolean existsByOperation(Operation operation);
}
