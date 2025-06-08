// src/App.jsx
import React, { useState, useEffect } from 'react';
import * as api from './apiService'; // Импортируем наш сервис
import './App.css'; // Базовые стили

function App() {
    const [userId, setUserId] = useState('user-123'); // ID пользователя по умолчанию
    const [balance, setBalance] = useState(null);
    const [orders, setOrders] = useState([]);
    const [depositAmount, setDepositAmount] = useState(100);
    const [message, setMessage] = useState('');

    const showMessage = (msg, isError = false) => {
        setMessage({ text: msg, error: isError });
        setTimeout(() => setMessage(''), 3000);
    };

    // --- Обработчики для счетов ---
    const handleCreateAccount = async () => {
        try {
            const response = await api.createAccount(userId);
            showMessage(`Счет для ${response.data.userId} создан!`);
            handleGetBalance();
        } catch (error) {
            showMessage(`Ошибка: ${error.response?.status === 409 ? 'Счет уже существует' : error.message}`, true);
        }
    };

    const handleGetBalance = async () => {
        if (!userId) return;
        try {
            const response = await api.getAccountBalance(userId);
            setBalance(response.data.balance);
        } catch (error) {
            setBalance(null);
            showMessage(`Ошибка получения баланса. Возможно, счета не существует.`, true);
        }
    };

    const handleDeposit = async () => {
        try {
            const response = await api.depositToAccount(userId, parseFloat(depositAmount));
            setBalance(response.data.balance);
            showMessage(`Счет пополнен на ${depositAmount}!`);
        } catch (error) {
            showMessage(`Ошибка пополнения: ${error.message}`, true);
        }
    };

    // --- Обработчики для заказов ---
    const handleCreateOrder = async () => {
        // Формируем данные в структуре, которую ожидает бэкенд
        const orderData = {
            items: [ // 1. Создаем ключ "items" со значением-массивом
                {
                    productId: `prod-${Date.now()}`,
                    quantity: 1,
                    pricePerUnit: Math.floor(Math.random() * 100) + 10 // 2. Переименовали "price" в "pricePerUnit"
                }
            ]
        };

        try {
            // Отправляем на бэкенд уже правильный объект
            await api.createOrder(userId, orderData);
            showMessage('Заказ успешно создан! (Обрабатывается асинхронно)');
            setTimeout(handleGetOrders, 2000); // Обновляем список через 2 сек
        } catch (error) {
            showMessage(`Ошибка создания заказа: ${error.message}`, true);
        }
    };

    const handleGetOrders = async () => {
        if (!userId) return;
        try {
            const response = await api.getUserOrders(userId);
            setOrders(response.data);
        } catch (error) {
            setOrders([]);
            showMessage(`Ошибка получения заказов: ${error.message}`, true);
        }
    };

    // Загрузка данных при изменении userId
    useEffect(() => {
        handleGetBalance();
        handleGetOrders();
    }, [userId]);

    return (
        <div className="App">
            <header className="App-header">
                <h1>Панель управления заказами и счетами</h1>
                <div className="user-input">
                    <label htmlFor="userId">ID Пользователя (X-User-Id): </label>
                    <input
                        id="userId"
                        type="text"
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                        placeholder="Введите User ID"
                    />
                </div>
                {message && <div className={`message ${message.error ? 'error' : 'success'}`}>{message.text}</div>}
            </header>

            <main className="container">
                <section className="card">
                    <h2>Управление счетом</h2>
                    <div className="balance-display">
                        Текущий баланс: <strong>{balance !== null ? `${balance.toFixed(2)} у.е.` : 'неизвестно'}</strong>
                    </div>
                    <div className="actions">
                        <button onClick={handleCreateAccount}>Создать счет</button>
                        <button onClick={handleGetBalance}>Обновить баланс</button>
                    </div>
                    <div className="actions">
                        <input
                            type="number"
                            value={depositAmount}
                            onChange={(e) => setDepositAmount(e.target.value)}
                        />
                        <button onClick={handleDeposit}>Пополнить счет</button>
                    </div>
                </section>

                <section className="card">
                    <h2>Управление Заказами</h2>
                    <div className="actions">
                        <button onClick={handleCreateOrder}>Создать новый случайный заказ</button>
                        <button onClick={handleGetOrders}>Обновить список заказов</button>
                    </div>

                    <div className="order-list">
                        <h3>Список заказов:</h3>
                        {orders.length > 0 ? (
                            <ul>
                                {orders.map(order => (
                                    <li key={order.id}>
                                        ID: {order.id},
                                        Статус: {order.status},
                                        Сумма: {order.totalAmount}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>Заказов нет.</p>
                        )}
                    </div>


                </section>
            </main>
        </div>
    );
}

export default App;