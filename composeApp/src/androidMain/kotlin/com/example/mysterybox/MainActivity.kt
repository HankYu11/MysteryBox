package com.example.mysterybox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.mysterybox.auth.setAppContext
import com.example.mysterybox.data.model.OAuthCallbackResult

class MainActivity : ComponentActivity() {

    private var oauthCallback by mutableStateOf<OAuthCallbackResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setAppContext(this)
        handleIntent(intent)

        setContent {
            App(
                oauthCallback = oauthCallback,
                onOAuthCallbackHandled = { oauthCallback = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "mysterybox" && uri.host == "auth") {
                val code = uri.getQueryParameter("code")
                val state = uri.getQueryParameter("state")
                val error = uri.getQueryParameter("error")

                oauthCallback = OAuthCallbackResult(
                    code = code,
                    state = state,
                    error = error
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
