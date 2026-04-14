package app.kotori.japanese.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kotori.japanese.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(uiState.text)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.onButtonClicked() }) {
            Text("Click Me")
        }
    }
}