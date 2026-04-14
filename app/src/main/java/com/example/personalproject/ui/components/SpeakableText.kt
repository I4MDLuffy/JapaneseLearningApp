package app.kotori.japanese.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Displays Japanese text with a small inline volume button that reads it aloud via TTS.
 * Pass the `speak` lambda from [app.kotori.japanese.util.rememberTts].
 */
@Composable
fun SpeakableText(
    text: String,
    speak: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    if (text.isBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Text(text = text, style = style, color = color)
        IconButton(
            onClick = { speak(text) },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.VolumeUp,
                contentDescription = "Pronounce",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            )
        }
    }
}
