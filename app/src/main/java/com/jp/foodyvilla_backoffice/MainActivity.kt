package com.jp.foodyvilla_backoffice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jp.foodyvilla_backoffice.fcm.createNotificationChannel
import com.jp.foodyvilla_backoffice.fcm.subscribeToTopic
import com.jp.foodyvilla_backoffice.presentation.new_backoffice.navigation.NewFoodyVillaNavGraph

import com.jp.foodyvilla_backoffice.presentation.utils.HideSystemBars
import com.jp.foodyvilla_backoffice.ui.theme.AppTheme
import com.razorpay.Checkout

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Checkout.preload(applicationContext)
        createNotificationChannel(this)
        subscribeToTopic("new_order")
        enableEdgeToEdge()
        setContent {
            AppTheme(dynamicColor = false) {
                HideSystemBars()
        Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    NewFoodyVillaNavGraph()
                }


            }
        }
    }



}
