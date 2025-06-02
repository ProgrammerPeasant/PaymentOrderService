package com.example.controller;

import com.example.dto.AccountBalanceResponse;
import com.example.dto.DepositRequest;
import com.example.model.Account;
import com.example.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class AccountController {
    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment account for the user",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountBalanceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user ID missing"),
                    @ApiResponse(responseCode = "409", description = "Account already exists for this user")
            })
    @PostMapping("/accounts")
    public ResponseEntity<AccountBalanceResponse> createAccount(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Account account = paymentService.createAccount(userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AccountBalanceResponse(account.getUserId(), account.getBalance()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @Operation(summary = "Deposit funds into a user's account",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deposit successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountBalanceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input, user ID missing, or account not found"),
            })
    @PostMapping("/accounts/deposit")
    public ResponseEntity<AccountBalanceResponse> depositToAccount(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody DepositRequest depositRequest) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Account account = paymentService.depositToAccount(userId, depositRequest.amount());
            return ResponseEntity.ok(new AccountBalanceResponse(account.getUserId(), account.getBalance()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get account balance for a user",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountBalanceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "User ID missing or account not found"),
            })
    @GetMapping("/accounts/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Account account = paymentService.getAccountBalance(userId);
            return ResponseEntity.ok(new AccountBalanceResponse(account.getUserId(), account.getBalance()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}