package app.kotori.japanese.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.ui.theme.JlptN1Color
import app.kotori.japanese.ui.theme.JlptN2Color
import app.kotori.japanese.ui.theme.JlptN3Color
import app.kotori.japanese.ui.theme.JlptN4Color
import app.kotori.japanese.ui.theme.JlptN5Color

@Composable
fun JlptBadge(level: String) {
    val color = when (level.uppercase()) {
        "N5" -> JlptN5Color
        "N4" -> JlptN4Color
        "N3" -> JlptN3Color
        "N2" -> JlptN2Color
        "N1" -> JlptN1Color
        else -> Color.Gray
    }
    Text(
        text = level,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
