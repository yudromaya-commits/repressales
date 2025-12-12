package com.example.repressales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repressales.repository.OrderRepository
import com.example.repressales.repository.ProductRepository

class DealViewModelFactory(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealViewModel::class.java)) {
            return DealViewModel(productRepository, orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}