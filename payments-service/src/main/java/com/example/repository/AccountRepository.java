package com.example.repository;

import com.example.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUserId(String userId);

    // cas операции
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.userId = :userId AND a.balance >= :amount")
    int debitBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    // для депчика
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.userId = :userId")
    int creditBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}