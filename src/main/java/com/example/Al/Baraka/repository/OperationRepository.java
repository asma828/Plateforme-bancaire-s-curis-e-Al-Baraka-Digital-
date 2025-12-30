package com.example.Al.Baraka.repository;


import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.Operation;
import com.example.Al.Baraka.enums.OperationStatus;
import com.example.Al.Baraka.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    List<Operation> findByStatus(OperationStatus status);

    List<Operation> findByAccountSource(Account account);

    List<Operation> findByAccountDestination(Account account);

    List<Operation> findByType(OperationType type);

    @Query("SELECT o FROM Operation o WHERE o.accountSource = :account OR o.accountDestination = :account ORDER BY o.createdAt DESC")
    List<Operation> findByAccountOrderByCreatedAtDesc(@Param("account") Account account);

    @Query("SELECT o FROM Operation o WHERE o.status = 'PENDING' ORDER BY o.createdAt ASC")
    List<Operation> findPendingOperations();

}
