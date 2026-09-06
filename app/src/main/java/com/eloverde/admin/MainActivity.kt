package com.eloverde.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eloverde.admin.presentation.AdminApp
import com.eloverde.admin.presentation.theme.EloVerdeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { EloVerdeTheme { AdminApp() } }
    }
}
