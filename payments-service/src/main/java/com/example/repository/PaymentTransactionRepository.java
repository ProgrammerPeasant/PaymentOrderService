package com.example.repository;

import com.example.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    boolean existsByOrderId(String orderId);
    Optional<PaymentTransaction> findByOrderId(String orderId);
}