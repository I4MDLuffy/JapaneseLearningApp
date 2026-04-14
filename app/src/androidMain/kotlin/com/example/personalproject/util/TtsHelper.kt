package app.kotori.japanese.util

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
actual fun rememberTts(): (String) -> Unit {
    val context = LocalContext.current
    val tts = remember { mutableListOf<TextToSpeech>() }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { /* ignore init status */ }
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
