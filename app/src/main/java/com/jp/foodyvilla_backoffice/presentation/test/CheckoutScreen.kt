package com.jp.foodyvilla_backoffice.presentation.test

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.razorpay.Checkout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// ─── Models ──────────────────────────────────────────────────────────────────

data class ProductOrder(
    val orderId: String,
    val productId: String,
    val productName: String,
    val amount: Int,
    val currency: String = "INR",
    val userEmail: String,
    val userPhone: String,
    val userName: String
)

data class PaymentResult(
    val razorpayPaymentId: String,
    val razorpayOrderId: String,
    val razorpaySignature: String,
    val productOrder: ProductOrder,
    val status: PaymentStatus,
    val errorCode: Int? = null,
    val errorDescription: String? = null
)

enum class PaymentStatus { SUCCESS, FAILED }

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class CheckoutUiState {
    data object Idle : CheckoutUiState()
    data object CreatingOrder : CheckoutUiState()
    data class ReadyToLaunchPayment(
        val razorpayOrderId: String,
        val productOrder: ProductOrder
    ) : CheckoutUiState()
    data class PaymentSuccess(val paymentResult: PaymentResult) : CheckoutUiState()
    data class PaymentFailed(val errorCode: Int, val errorDescription: String) : CheckoutUiState()
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var pendingOrder: ProductOrder? = null

    fun initiatePayment(productOrder: ProductOrder) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.CreatingOrder
            pendingOrder = productOrder

            // TODO: Replace with real backend call to create Razorpay order
            val razorpayOrderId = "order_mock_${System.currentTimeMillis()}"

            _uiState.value = CheckoutUiState.ReadyToLaunchPayment(
                razorpayOrderId = razorpayOrderId,
                productOrder = productOrder
            )
        }
    }

    fun onPaymentSuccess(
        razorpayPaymentId: String,
        razorpayOrderId: String,
        razorpaySignature: String
    ) {
        val order = pendingOrder ?: return
        viewModelScope.launch {
            // TODO: Save transaction to your DB / backend here
            _uiState.value = CheckoutUiState.PaymentSuccess(
                PaymentResult(
                    razorpayPaymentId = razorpayPaymentId,
                    razorpayOrderId = razorpayOrderId,
                    razorpaySignature = razorpaySignature,
                    productOrder = order,
                    status = PaymentStatus.SUCCESS
                )
            )
            pendingOrder = null
        }
    }

    fun onPaymentError(errorCode: Int, errorDescription: String) {
        _uiState.value = CheckoutUiState.PaymentFailed(errorCode, errorDescription)
        pendingOrder = null
    }

    fun resetState() {
        _uiState.value = CheckoutUiState.Idle
    }
}

// ─── Composable ──────────────────────────────────────────────────────────────

@Composable
fun CheckoutScreen(viewModel: CheckoutViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sampleOrder = ProductOrder(
        orderId = "ORD_${System.currentTimeMillis()}",
        productId = "PROD_001",
        productName = "Premium Subscription",
        amount = 49900,
        userEmail = "user@example.com",
        userPhone = "9876543210",
        userName = "John Doe"
    )

    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.ReadyToLaunchPayment) {
//            val state = uiState as CheckoutUiState.ReadyToLaunchPayment
//            val checkout = Checkout()
//            checkout.setKeyID("rzp_test_Sfjnut1cTiybCd") // 🔑 Replace
//            val options = JSONObject().apply {
//                put("name", "Your App Name")
//                put("description", state.productOrder.productName)
////                put("order_id", state.razorpayOrderId)
//                put("currency", state.productOrder.currency)
//                put("amount", state.productOrder.amount)
//                put("prefill", JSONObject().apply {
//                    put("name", state.productOrder.userName)
//                    put("email", state.productOrder.userEmail)
//                    put("contact", state.productOrder.userPhone)
//                })
//                put("theme", JSONObject().apply { put("color", "#E23744") })
//            }
//            checkout.open(context as Activity, options)

            val checkout = Checkout()
            checkout.setKeyID("rzp_test_ShBw7mlCM6gT6y") // ✅ dummy test key

            val options = JSONObject().apply {
                put("name", "FoodyVilla") // App name
                put("description", "Premium Subscription")
                put("currency", "INR")
                put("amount", 49900) // ₹499.00 (amount in paise)
                    put("theme.color", "#E23744")

                put("prefill", JSONObject().apply {
                    put("name", "John Doe")
                    put("email", "test@razorpay.com")
                    put("contact", "9876543210")
                })



            }

            checkout.open(context as Activity, options)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { viewModel.initiatePayment(sampleOrder) }) {
            Text("Pay ₹499")
        }
    }
}
