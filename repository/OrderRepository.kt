
package com.example.repressales.repository

import android.util.Log
import com.example.repressales.api.TaskApi
import com.example.repressales.model.CreateOrderResponse
import com.example.repressales.model.Order

class OrderRepository {
    private val apiService = TaskApi.service

    suspend fun createOrder(order: Order): CreateOrderResponse {
        return try {
            val response = apiService.createOrder(order)
            if (response.isSuccessful) {
                response.body() ?: CreateOrderResponse(
                    success = false,
                    orderId = null,
                    message = null,
                    error = "Пустой ответ от сервера"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "Ошибка создания заказа: ${response.code()} - $errorBody")
                CreateOrderResponse(
                    success = false,
                    orderId = null,
                    message = null,
                    error = "Ошибка сервера: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Ошибка сети: ${e.message}")
            CreateOrderResponse(
                success = false,
                orderId = null,
                message = null,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }
}
