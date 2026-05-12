package com.jp.foodyvilla_backoffice.presentation.screens.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.compose.koinViewModel

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken("936302589972-bjm4nnn6sie66eaqalfi0l6re6ja7fju.apps.googleusercontent.com") // from Firebase / Google Cloud
        .build()

    return GoogleSignIn.getClient(context, gso)
}

@Composable
fun GoogleSignInScreen(loginViewModel: LoginViewModel = koinViewModel()) {

    val context = LocalContext.current
    val googleSignInClient = remember {
        getGoogleSignInClient(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                val email = account.email
                if (!idToken.isNullOrEmpty()) {
                    loginViewModel.signInWithSupabase(idToken)
                } else {
                    Log.e("GoogleSignIn", "ID Token is null")
                }
                Log.d("GoogleSignIn", "Success: $email")
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Failed: ${e.statusCode}")
            }
        }
    }

    Box(modifier = Modifier. fillMaxSize(), contentAlignment = Alignment.Center){
        GoogleSignInButton {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = "Sign in with Google")
    }
}