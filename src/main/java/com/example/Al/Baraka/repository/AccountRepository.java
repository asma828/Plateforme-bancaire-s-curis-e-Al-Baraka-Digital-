package com.example.Al.Baraka.repository;


import com.example.Al.Baraka.model.Account;
import com.example.Al.Baraka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByOwner(User owner);

    boolean existsByAccountNumber(String accountNumber);
}
