package app.kotori.japanese.util

import androidx.compose.runtime.Composable

/**
 * Returns a speak lambda that reads Japanese text aloud.
 * Platform implementations: Android uses TextToSpeech, iOS uses AVSpeechSynthesizer.
 */
@Composable
expect fun rememberTts(): (String) -> Unit
