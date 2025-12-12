package com.example.repressales.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("Клиент")
    val clientId: String? = null,

    @SerializedName("Комментарий")
    val comment: String? = null,

    @SerializedName("Товары")
    val products: List<OrderProduct>
)

data class OrderProduct(
    @SerializedName("Товар")
    val productId: String,

    @SerializedName("Количество")
    val quantity: Int,

    @SerializedName("Цена")
    val price: Double,

    @SerializedName("Сумма")
    val total: Double
)

// Ответ от сервера при создании заказа
data class CreateOrderResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("id")
    val orderId: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("error")
    val error: String?
)