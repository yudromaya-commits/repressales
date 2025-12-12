package com.example.repressales.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repressales.viewmodel.DealViewModel
import com.example.repressales.viewmodel.DealViewModelFactory
import com.example.repressales.repository.ProductRepository
import com.example.repressales.repository.OrderRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealView(
    modifier: Modifier = Modifier
) {
    val productRepository = remember { ProductRepository() }
    val orderRepository = remember { OrderRepository() }

    val viewModel: DealViewModel = viewModel(
        factory = DealViewModelFactory(productRepository, orderRepository)
    )

    val uiState by viewModel.uiState.collectAsState()

    val totalCartItems = uiState.cartItems.sumOf { it.quantity }
    val totalCartPrice = uiState.cartItems.sumOf { it.totalPrice }

    // Управление отображением корзины
    var showCart by remember { mutableStateOf(false) }
    var showCheckout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Сделки",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    // Иконка корзины с количеством товаров
                    Box {
                        IconButton(
                            onClick = { showCart = true },
                            enabled = uiState.cartItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Корзина"
                            )
                        }
                        if (totalCartItems > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            ) {
                                Text(totalCartItems.toString())
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.cartItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCheckout = true },
                    icon = {
                        Icon(Icons.Default.CheckCircle, "Оформить заказ")
                    },
                    text = {
                        Text("Оформить (${String.format("%,.0f ₽", totalCartPrice)})")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Панель поиска и фильтров
            DealFiltersPanel(
                searchQuery = uiState.searchQuery,
                selectedCategory = uiState.selectedCategory,
                categories = viewModel.categories,
                filteredProductsCount = uiState.filteredProducts.size, // Правильное количество
                onSearchQueryChange = viewModel::updateSearchQuery,
                onCategorySelect = viewModel::selectCategory,
                onRefresh = viewModel::loadProducts
            )

            // Список товаров
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = viewModel::loadProducts
                )
            } else if (uiState.filteredProducts.isEmpty()) {
                EmptyState(
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedCategory,
                    onClearFilters = {
                        viewModel.updateSearchQuery("")
                        viewModel.selectCategory(null)
                    }
                )
            } else {
                ProductGrid(
                    products = uiState.filteredProducts,
                    cartItems = uiState.cartItems,
                    onAddToCart = viewModel::addToCart
                )
            }
        }

        // Модальные окна
        if (showCart) {
            CartModal(
                cartItems = uiState.cartItems,
                totalPrice = totalCartPrice,
                onUpdateQuantity = viewModel::updateCartItemQuantity,
                onRemoveItem = viewModel::removeFromCart,
                onClearCart = viewModel::clearCart,
                onDismiss = { showCart = false },
                onCheckout = {
                    showCart = false
                    showCheckout = true
                }
            )
        }

        if (showCheckout) {
            CheckoutModal(
                cartItems = uiState.cartItems,
                totalPrice = totalCartPrice,
                comment = uiState.orderComment,
                isCreatingOrder = uiState.isCreatingOrder,
                orderSuccess = uiState.orderSuccess,
                orderMessage = uiState.orderMessage,
                onCommentChange = viewModel::updateOrderComment,
                onCreateOrder = viewModel::createOrder,
                onDismiss = {
                    showCheckout = false
                    if (uiState.orderSuccess == true) {
                        viewModel.clearOrderStatus()
                    }
                }
            )
        }
    }
}
@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "Ошибка",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Повторить")
        }
    }
}

@Composable
fun EmptyState(
    searchQuery: String,
    selectedCategory: String?,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = "Нет товаров",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isNotEmpty() || selectedCategory != null) {
                "Товары не найдены"
            } else {
                "Товары отсутствуют"
            },
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "По запросу \"$searchQuery\" ничего не найдено"
            } else if (selectedCategory != null) {
                "В категории \"$selectedCategory\" товаров нет"
            } else {
                "Загрузите товары с сервера"
            },
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (searchQuery.isNotEmpty() || selectedCategory != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearFilters) {
                Icon(Icons.Default.ClearAll, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сбросить фильтры")
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<com.example.repressales.model.Product>,
    cartItems: List<com.example.repressales.model.CartItem>,
    onAddToCart: (com.example.repressales.model.Product) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onAddToCart = { onAddToCart(product) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}