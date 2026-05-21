package com.jp.foodyvilla_backoffice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jp.foodyvilla_backoffice.fcm.createNotificationChannel
import com.jp.foodyvilla_backoffice.fcm.subscribeToTopic
import com.jp.foodyvilla_backoffice.presentation.navigation.FoodyVillaNavGraph
import com.jp.foodyvilla_backoffice.presentation.screens.home.HomeViewModel
import com.jp.foodyvilla_backoffice.presentation.utils.HideSystemBars
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlin.getValue

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)
        createNotificationChannel(this)
        subscribeToTopic("new_order")
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                HideSystemBars()

                FoodyVillaNavGraph()
//
////                MobileLoginScreen { }
//
////                OtpVerificationScreen {  }
//            val context = LocalContext.current
//                GoogleSignInScreen()


//                CheckoutScreen(viewModel = viewModel)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Checkout.clearUserData(this)
    }
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
       println("Success Payment $razorpayPaymentId")

        println("Success Data $paymentData")

        viewModel.onPaymentSuccess(
            razorpayPaymentId = razorpayPaymentId ?: "",
            razorpayOrderId = paymentData?.orderId ?: "",
            razorpaySignature = paymentData?.signature ?: ""
        )
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, p2: PaymentData?) {
        println("Error Payment $errorCode   $errorDescription")

        println("Error Data $p2")

        viewModel.onPaymentError(
            errorCode = errorCode,
            errorDescription = errorDescription ?: "Unknown error"
        )
    }
}
