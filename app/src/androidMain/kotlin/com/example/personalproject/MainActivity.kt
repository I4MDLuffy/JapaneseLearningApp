package app.kotori.japanese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialise the Android platform singleton before anything else.
        AndroidPlatform.init(applicationContext)
        appContainer = AppContainer()
        setContent {
            App(appContainer)
        }
    }
}
