package app.kotori.japanese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import app.kotori.japanese.ui.screen.HomeScreen
import app.kotori.japanese.ui.theme.PersonalProjectTheme
import app.kotori.japanese.viewmodel.HomeViewModel

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