package com.jp.foodyvilla_backoffice.fcm

import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

fun subscribeToTopic(topic: String) {
    FirebaseMessaging.getInstance()
        .subscribeToTopic(topic)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Subscribed to $topic")
            } else {
                Log.d("FCM", "Subscription failed")
            }
        }
}