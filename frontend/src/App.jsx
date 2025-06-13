import React, { useState, useEffect, useRef } from 'react';
import * as api from './apiService';
import './App.css';

import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function App() {
    const [userId, setUserId] = useState('user-123');
    const [balance, setBalance] = useState(null);
    const [orders, setOrders] = useState([]);
    const [depositAmount, setDepositAmount] = useState(100);
    const ws = useRef(null);

    const showMessage = (msg, isError = false) => {
        if (isError) {
            toast.error(msg);
        } else {
            toast.success(msg);
        }
    };

    useEffect(() => {
        if (!userId) {
            if (ws.current) {
                console.log('Closing WebSocket due to empty userId.');
                ws.current.close();
                ws.current = null;
            }
            return;
        }

        if (ws.current) {
            console.log('Closing previous WebSocket connection.');
            ws.current.close();
        }

        const wsUrl = `ws://localhost:8080/ws/order-status?userId=${encodeURIComponent(userId)}`;
        console.log(`Attempting to connect to WebSocket: ${wsUrl}`);
        ws.current = new WebSocket(wsUrl);

        ws.current.onopen = () => {
            console.log(`WebSocket connected for userId: ${userId}`);
            toast.info(`🔔 Connected for order updates (User: ${userId})`);
        };

        ws.current.onmessage = (event) => {
            console.log('WebSocket message received:', event.data);
            try {
                const orderUpdate = JSON.parse(event.data);

                const notificationMessage = `Заказ #${orderUpdate.id.substring(0, 8)} обновлен: ${orderUpdate.status}`;
                toast.success(notificationMessage);

                setOrders(prevOrders => {
                    const orderExists = prevOrders.find(o => o.id === orderUpdate.id);
                    if (orderExists) {
                        return prevOrders.map(o => o.id === orderUpdate.id ? {...o, ...orderUpdate} : o);
                    } else {
                        return prevOrders;
                    }
                });

            } catch (error) {
                console.error('Failed to parse WebSocket message or show toast:', error);
                toast.error("Ошибка обработки обновления заказа.");
            }
        };

        ws.current.onerror = (error) => {
            console.error('WebSocket error:', error);
            toast.error('Ошибка WebSocket соединения.');
        };

        ws.current.onclose = (event) => {
            console.log(`WebSocket disconnected for userId: ${userId}. Clean: ${event.wasClean}, Code: ${event.code}, Reason: ${event.reason}`);
            if (!event.wasClean) {
                toast.warn(`Соединение для обновлений потеряно (User: ${userId})`);
            }
            ws.current = null;
        };

        return () => {
            if (ws.current) {
                console.log(`Closing WebSocket connection for userId: ${userId} on cleanup.`);
                ws.current.close();
                ws.current = null;
            }
        };
    }, [userId]);

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
            // eslint-disable-next-line no-unused-vars
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

    const handleCreateOrder = async () => {
        const orderData = {
            items: [{
                productId: `prod-${Date.now()}`,
                quantity: 1,
                pricePerUnit: Math.floor(Math.random() * 100) + 10
            }]
        };
        try {
            await api.createOrder(userId, orderData);
            showMessage('Заказ успешно создан! (Ожидайте уведомление о статусе)');
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

    useEffect(() => {
        if (userId) {
            handleGetBalance();
            handleGetOrders();
        } else {
            setBalance(null);
            setOrders([]);
        }
    }, [userId]);

    return (
        <div className="App">
            <ToastContainer
                position="top-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme="colored"
            />

            <header className="App-header">
                <h1>Панель управления заказами и счетами</h1>
                <div className="user-input">
                    <label htmlFor="userId">ID Пользователя: </label>
                    <input
                        id="userId"
                        type="text"
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                        placeholder="Введите User ID"
                    />
                </div>

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
                                        ID: {order.id.substring(0,8)}...,
                                        Статус: <strong style={{color: order.status === 'PAID' ? 'green' : (order.status === 'PAYMENT_FAILED' ? 'red' : 'black')}}>{order.status}</strong>,
                                        Сумма: {order.totalAmount}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p>Заказов нет или пользователь не указан.</p>
                        )}
                    </div>
                </section>
            </main>
        </div>
    );
}

export default App;