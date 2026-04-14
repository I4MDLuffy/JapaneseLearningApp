package com.example.personalproject.util

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Returns a speak lambda that uses Android TTS to read Japanese text aloud.
 * The TTS engine is initialised lazily and cleaned up when the composable leaves composition.
 */
@Composable
fun rememberTts(): (String) -> Unit {
    val context = LocalContext.current
    val tts = remember { mutableListOf<TextToSpeech>() }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { /* init — ignore status, speak() is no-op if not ready */ }
        engine.language = Locale.JAPANESE
        tts.add(engine)
        onDispose {
            tts.forEach { it.stop(); it.shutdown() }
            tts.clear()
        }
    }

    return { text: String ->
        tts.firstOrNull()?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
