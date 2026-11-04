package com.payra.calling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.payra.calling.designsystem.PayraTheme
import com.payra.calling.navigation.PayraNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayraTheme {
                PayraNavHost()
            }
        }
    }
}
