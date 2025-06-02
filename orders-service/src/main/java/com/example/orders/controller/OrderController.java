package com.example.orders.controller;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.service.OrderService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Create a new order",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or user ID missing")
            })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest createOrderRequest) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build(); // Or a more specific error DTO
        }
        try {
            OrderResponse orderResponse = orderService.createOrder(userId, createOrderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
        } catch (RuntimeException e) { // Catch specific exceptions if needed
            return ResponseEntity.badRequest().body(null); // Or error DTO
        }
    }

    @Operation(summary = "Get list of orders for the user",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse[].class))),
                    @ApiResponse(responseCode = "400", description = "User ID missing")
            })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<OrderResponse> orders = orderService.getOrdersForUser(userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get a specific order by ID for the user",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true, description = "User ID"),
                    @Parameter(in = ParameterIn.PATH, name = "orderId", required = true, description = "Order ID")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "User ID missing"),
                    @ApiResponse(responseCode = "404", description = "Order not found for this user")
            })
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return orderService.getOrderByIdAndUser(orderId, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}