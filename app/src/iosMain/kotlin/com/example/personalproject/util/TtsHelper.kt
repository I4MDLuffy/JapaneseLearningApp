package app.kotori.japanese.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate

@Composable
actual fun rememberTts(): (String) -> Unit {
    val synthesizer = remember { AVSpeechSynthesizer() }
    return { text: String ->
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("ja-JP")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        if (synthesizer.isSpeaking) synthesizer.stopSpeakingAtBoundary(0)
        synthesizer.speakUtterance(utterance)
    }
}
