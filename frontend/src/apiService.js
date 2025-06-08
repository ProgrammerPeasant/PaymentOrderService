// src/apiService.js
import axios from 'axios';

// Создаем инстанс axios с базовым URL вашего API Gateway
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/v1', // Указываем на API Gateway
    headers: {
        'Content-Type': 'application/json'
    }
});

// --- Функции для работы со счетами (Payments) ---

export const createAccount = (userId) => {
    return apiClient.post('/payments/accounts', null, {
        headers: { 'X-User-Id': userId }
    });
};

export const getAccountBalance = (userId) => {
    return apiClient.get('/payments/accounts/balance', {
        headers: { 'X-User-Id': userId }
    });
};

export const depositToAccount = (userId, amount) => {
    return apiClient.post('/payments/accounts/deposit', { amount }, {
        headers: { 'X-User-Id': userId }
    });
};

// --- Функции для работы с заказами (Orders) ---

export const createOrder = (userId, orderData) => {
    // orderData должен быть объектом, например { productId: "p1", quantity: 2, price: 150.0 }
    return apiClient.post('/orders', orderData, {
        headers: { 'X-User-Id': userId }
    });
};

export const getUserOrders = (userId) => {
    return apiClient.get('/orders', {
        headers: { 'X-User-Id': userId }
    });
};

export const getOrderById = (userId, orderId) => {
    return apiClient.get(`/orders/${orderId}`, {
        headers: { 'X-User-Id': userId }
    });
};