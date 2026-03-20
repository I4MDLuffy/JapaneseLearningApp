package com.example.personalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.personalproject.ui.screen.HomeScreen
import com.example.personalproject.ui.theme.PersonalProjectTheme
import com.example.personalproject.viewmodel.HomeViewModel

class FirstScreen : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PersonalProjectTheme {
                HomeScreen(viewModel)
            }
        }
    }
}