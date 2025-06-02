package com.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "payments_accounts")
@Getter @Setter @NoArgsConstructor
public class Account {
    @Id
    private String userId; // ключ

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Version
    private Long version;

    public Account(String userId, BigDecimal initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
    }
}