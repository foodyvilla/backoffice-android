package com.jp.foodyvilla_backoffice.presentation.screens.home

import Banner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.jp.foodyvilla_backoffice.data.model.Category
import com.jp.foodyvilla_backoffice.data.model.FoodItem
import com.jp.foodyvilla_backoffice.data.model.MockData
import com.jp.foodyvilla_backoffice.data.model.cart.CartItem
import com.jp.foodyvilla_backoffice.data.model.order.OrderModel
import com.jp.foodyvilla_backoffice.data.model.user.UserProfile
import com.jp.foodyvilla_backoffice.data.repo.CartRepository
import com.jp.foodyvilla_backoffice.data.repo.LocationRepository
import com.jp.foodyvilla_backoffice.data.repo.OfferRepo
import com.jp.foodyvilla_backoffice.data.repo.OrderRepository
import com.jp.foodyvilla_backoffice.data.repo.ProductRepo
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val allItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val bestSellers: List<FoodItem>
        get() = allItems.filter { it.isBestSeller }


    val filteredBestSellersItems: List<FoodItem>
        get() {
            val query = searchQuery.trim()

            return allItems.filter { it.isBestSeller }.filter { item ->

                val matchesSearch =
                    query.isBlank() ||
                            item.name.contains(query, true) ||
                            item.description.contains(query, true) ||
                            item.category.contains(query, true) ||
                            item.prepTime.contains(query, true) ||
                            item.price.toString().contains(query) ||
                            item.rating.toString().contains(query) ||
                            (query.equals("veg", true) && item.isVeg) ||
                            (query.equals("vegan", true) && item.isVegan)

                val matchesCategory =
                    selectedCategory == "all" ||
                            item.category.equals(selectedCategory, true) ||
                            // 🔥 ALSO allow category match via search
                            item.name.contains(selectedCategory, true) ||
                            item.description.contains(selectedCategory, true)

                matchesSearch && matchesCategory
            }
        }
    val filteredItems: List<FoodItem>
        get() {
            val query = searchQuery.trim()

            return allItems.filter { item ->

                val matchesSearch =
                    query.isBlank() ||
                            item.name.contains(query, true) ||
                            item.description.contains(query, true) ||
                            item.category.contains(query, true) ||
                            item.prepTime.contains(query, true) ||
                            item.price.toString().contains(query) ||
                            item.rating.toString().contains(query) ||
                            (query.equals("veg", true) && item.isVeg) ||
                            (query.equals("vegan", true) && item.isVegan)

                val matchesCategory =
                    selectedCategory == "all" ||
                            item.category.equals(selectedCategory, true) ||
                            // 🔥 ALSO allow category match via search
                            item.name.contains(selectedCategory, true) ||
                            item.description.contains(selectedCategory, true)

                matchesSearch && matchesCategory
            }
        }

    val popularItems: List<FoodItem>
        get() = allItems.filter { !it.isBestSeller }
}


data class OrderUiState(
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val instructions: String = "",
    val orderType: String = "Delivery",
    val lat: Double = 0.0,
    val long: Double = 0.0
)

class HomeViewModel(
    private val offerRepo: OfferRepo,
    private val productRepo: ProductRepo,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val locationRepository: LocationRepository
) :
    ViewModel() {


    private val _selectedPage = MutableStateFlow(0)
    val selectedPage = _selectedPage.asStateFlow()
    fun updateSelectedPage(page: Int) {
        _selectedPage.value = page
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    private val _orderState = MutableStateFlow(OrderUiState())
    val orderState: StateFlow<OrderUiState> = _orderState

    fun updateCustomerName(value: String) {
        _orderState.update { it.copy(customerName = value) }
    }

    fun updatePhone(value: String) {
        _orderState.update { it.copy(phone = value) }
    }

    fun updateAddress(value: String) {
        _orderState.update { it.copy(address = value) }
    }

    fun updateInstructions(value: String) {
        _orderState.update { it.copy(instructions = value) }
    }

    fun updateOrderType(value: String) {
        _orderState.update { it.copy(orderType = value) }
    }

    fun updateCustomerDetailsForOrder(user: UserProfile) {
        viewModelScope.launch {
            _orderState.update {
                it.copy(
                    customerName = user.name ?: "",
                    address = user.address ?: "",
                    phone = user.phone ?: "",
                    lat = user.lat ?: 0.0,
                    long = user.long ?: 0.0
                )
            }
        }
    }

    private val _locationState =
        MutableStateFlow<UiState<Pair<Double, Double>>>(UiState.Idle)

    val locationState = _locationState.asStateFlow()

    fun fetchCurrentLocation() {

        viewModelScope.launch {

            _locationState.value = UiState.Loading

            val result = locationRepository.fetchLocation()

            println("Location fetch $result")
            result.onSuccess { location ->

                _locationState.value = UiState.Success(location)
                val addressResult =
                    locationRepository.getAddressFromLocation(
                        latitude = location.first,
                        longitude = location.second
                    )

                println("Location address fetch $addressResult")


                val address = addressResult.getOrNull() ?: ""
                _orderState.update { state ->


                    state.copy(address = address, lat = location.first, long = location.second)


                }
            }

            result.onFailure { exception ->

                _locationState.value =
                    UiState.Error(
                        Exception(exception)
                    )
            }
        }
    }
    fun hasLocationPermission(): Boolean {
        return locationRepository.hasLocationPermission()
    }

    fun isGpsEnabled(): Boolean {
        return locationRepository.isGpsEnabled()
    }
    val fcm = FirebaseMessaging.getInstance()


    init {
        loadData()
        fcm.subscribeToTopic("offers")
        fcm.token
            .addOnSuccessListener { token ->
                println("FCM Token: $token")
            }
            .addOnFailureListener {
                println("Failed to get FCM token")
            }

    }


    private val _orderHistoryState = MutableStateFlow<UiState<List<OrderModel>>>(UiState.Idle)
    val orderHistoryState = _orderHistoryState.asStateFlow()
    fun getOrderedItems() {
        viewModelScope.launch {
            orderRepository.observeOrders().collectLatest {
                println("orders : $it")
                _orderHistoryState.value = it
            }

        }
    }

    fun getCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { res ->

                println(" cart items : $res")
                if (res is UiState.Success) {
                    _uiState.value = _uiState.value.copy(cartItems = res.data)

                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            offerRepo.getBanners().collect { items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        banners = items,
                        categories = MockData.categories
                    )
                }
            }

            getProduct()
            getCartItems()
            getOrderedItems()
        }
    }

    fun getCurrentLocation(): Flow<UiState<Pair<Double, Double>>> = flow {
        emit(UiState.Loading)

        try {
            val location = locationRepository.getCurrentLocation()

            if (location != null) {
                emit(UiState.Success(location))
            } else {
                emit(UiState.Error(Exception("Location not available")))
            }

        } catch (e: Exception) {
            emit(UiState.Error(e))
        }
    }


    // ─────────────────────────────────────────
// 🚀 PLACE ORDER
// ─────────────────────────────────────────


    private val _paymentState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val paymentState = _paymentState.asStateFlow()
    fun onPaymentSuccess(
        razorpayPaymentId: String,
        razorpayOrderId: String,
        razorpaySignature: String
    ) {
        viewModelScope.launch {


            println("Payment Success $razorpayPaymentId  $razorpaySignature  $razorpayOrderId")
            // TODO: Save transaction to your DB / backend here
//            _uiState.value = CheckoutUiState.PaymentSuccess(
//                PaymentResult(
//                    razorpayPaymentId = razorpayPaymentId,
//                    razorpayOrderId = razorpayOrderId,
//                    razorpaySignature = razorpaySignature,
//                    productOrder = order,
//                    status = PaymentStatus.SUCCESS
//                )
//            )
//            pendingOrder = null

            println("razorpayPaymentId $razorpayPaymentId")
            println("razorpayPaymentId $razorpayPaymentId")
            println("razorpayPaymentId $razorpayPaymentId")
            placeOrder(transactionId = razorpayPaymentId)
        }
    }


    fun onPaymentError(errorCode: Int, errorDescription: String) {

        println("Payment Error $errorCode  $errorDescription ")


//        _uiState.value = CheckoutUiState.PaymentFailed(errorCode, errorDescription)
//        pendingOrder = null


    }


    fun placeOrder(
        transactionId: String
    ) {
        val cartItems = _uiState.value.cartItems


        println("Place order called with transactionId $transactionId 1")
        if (cartItems.isEmpty()) {
//            _uiState.update { it.copy(orderError = "Cart is empty") }
            return
        }

        viewModelScope.launch {

//            getCurrentLocation().collectLatest {
////                println("Location $it")
//                if (it is UiState.Success) {
            orderRepository.placeOrder(
                cartItems = cartItems,
                address = _orderState.value.address,
                phone = _orderState.value.phone,
                customerName = _orderState.value.customerName,
                instruction = _orderState.value.instructions,
                orderType = _orderState.value.orderType,
                transactionId = transactionId,
//                        lat = it.data.first,
//                        long = it.data.second
            ).collectLatest { state ->

                println("Order Place State $state")
                when (state) {
                    is UiState.Loading -> {
//                        _uiState.update {
//                            it.copy(
//                                isPlacingOrder = true,
//                                orderError = null,
//                                orderSuccess = null
//                            )
//                        }
                    }

                    is UiState.Success -> {
                        _uiState.update {
                            it.copy(
                                cartItems = emptyList()
                            )
                        }
                    }

                    is UiState.Error -> {
                        _uiState.update {
                            it.copy(
//                                isPlacingOrder = false,
//                                orderError = state.exception.message ?: "Order failed"
                            )
                        }
                    }

//
                    else -> {}
                }
            }
        }
    }

    // ─────────────────────────────────────────
// 🧹 CLEAR ORDER STATE (call after showing snackbar/dialog)
// ─────────────────────────────────────────
    fun clearOrderState() {
//        _uiState.update {
////            it.copy(
////                orderSuccess = null,
////                orderError = null
////            )
//        }
    }

    fun updateCartItemQuantity(item: FoodItem, quantity: Int = 1) {

        if (quantity == 0) {
            _uiState.update { state ->
                state.copy(
                    cartItems = state.cartItems.filter { it.product_id != item.id }
                )
            }
        } else {
            _uiState.update { state ->

                val exists = state.cartItems.any { it.product_id == item.id }

                val updatedList = if (exists) {
                    state.cartItems.map {
                        if (it.product_id == item.id) {
                            it.copy(qty = quantity) // ✅ update in place
                        } else it
                    }
                } else {
                    state.cartItems + CartItem(
                        id = item.id,
                        products = item,
                        qty = quantity,
                        customer_id = 0,
                        product_id = item.id
                    )
                }

                state.copy(cartItems = updatedList)
            }
        }

        viewModelScope.launch {
            cartRepository.addToCart(item.id, quantity).collectLatest {
                println("update cart $it")
            }
        }
    }

    fun updateCartItemQuantity0(item: FoodItem, quantity: Int = 1) {
        if (quantity == 0) {
            _uiState.update { it ->
                it.copy(cartItems = it.cartItems.filter { it.product_id != item.id })
            }
        } else {
            _uiState.update {
                it.copy(cartItems = it.cartItems.filter { it.product_id != item.id } + CartItem(
                    id = item.id,
                    products = item,
                    qty = quantity,
                    customer_id = 0,
                    product_id = item.id
                ))
            }
        }

        viewModelScope.launch {
            cartRepository.addToCart(item.id, quantity).collectLatest {
                println("update cart $it")
            }
        }


    }

    fun getTotalCartValue(): Double {
        return uiState.value.cartItems.sumOf { it.totalPrice ?: 0.0 }
    }

    fun removeFromCart(itemId: Int) {
        viewModelScope.launch {
            cartRepository.addToCart(itemId, 0).collectLatest { res ->
                println("update cart $res")
                if (res is UiState.Success) {
                    _uiState.update {
                        it.copy(cartItems = it.cartItems.filter { it.products?.id != itemId })
                    }
                }
            }
        }


    }


    fun generateWhatsAppMessage(
        state: HomeUiState,
        name: String,
        phone: String,
        address: String,
        type: String,
        instructions: String
    ): String {

//        val orderId = System.currentTimeMillis().toString().takeLast(6)
//
//        val itemsText = state.cartItems.joinToString("\n") {
//            "${it.foodItem.name} x${it.quantity} = ₹${"%.2f".format(it.foodItem.price * it.quantity)}"
//        }
//
//        val total = state.cartItems.sumOf {
//            it.foodItem.price * it.quantity
//        }

        return """
🧾 *New Order*

📦 Order ID: orderId
👤 Name: $name
📞 Phone: $phone
🍽 Type: $type

📍 Address: ${if (type == "Delivery") address else "N/A"}

🛒 *Items:*
itemsText

💰 *Total: *

📝 Instructions:
$instructions
""".trimIndent()
    }

    fun getProduct() {
        viewModelScope.launch {
            try {
                productRepo.getProducts().collect { items ->

                    println("Items : $items")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allItems = items
                        )
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
