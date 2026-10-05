package com.example.shopeeafftracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.shopeeafftracker.ui.screens.HomeScreen
import com.example.shopeeafftracker.ui.theme.ShopeeAffTrackerTheme
import com.example.shopeeafftracker.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopeeAffTrackerTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
