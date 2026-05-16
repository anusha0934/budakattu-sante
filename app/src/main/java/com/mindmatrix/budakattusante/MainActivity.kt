package com.mindmatrix.budakattusante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mindmatrix.budakattusante.ui.BudakattuSanteApp
import com.mindmatrix.budakattusante.ui.theme.BudakattuTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Requirement 19: Entry point for Hilt DI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BudakattuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BudakattuSanteApp()
                }
            }
        }
    }
}
