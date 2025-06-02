package com.example.orders.repository;

import com.example.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Order> findByIdAndUserId(String id, String userId);
}