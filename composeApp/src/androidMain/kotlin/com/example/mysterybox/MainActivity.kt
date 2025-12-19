package com.example.mysterybox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mysterybox.auth.LineSdkLoginHelper
import com.example.mysterybox.auth.LineSdkLoginManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register this activity with LINE SDK login manager
        LineSdkLoginManager.setActivity(this)

        setContent {
            App()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle LINE SDK login result
        if (requestCode == LineSdkLoginHelper.REQUEST_CODE_LINE_LOGIN) {
            LineSdkLoginManager.handleLoginResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LineSdkLoginManager.clearActivity()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
