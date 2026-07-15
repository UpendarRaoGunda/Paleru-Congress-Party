package com.paleru.congress

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paleru.congress.ui.PaleruCongressApp
import com.paleru.congress.ui.theme.PaleruCongressTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            PaleruCongressTheme {
                PaleruCongressApp()
            }
        }
    }
}
