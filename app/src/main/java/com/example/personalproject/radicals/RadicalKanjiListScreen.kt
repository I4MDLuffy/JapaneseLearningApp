package app.kotori.japanese.radicals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.ui.components.KotobaTopBar

@Composable
fun RadicalKanjiListScreen(
    radicalId: String,
    onBack: () -> Unit,
    onKanjiClick: (String) -> Unit,
) {
    val container = LocalAppContainer.current

    data class ScreenState(val radical: RadicalEntry?, val kanji: List<KanjiEntry>)

    val state by produceState(ScreenState(null, emptyList())) {
        val radical = container.radicalRepository.getRadicalById(radicalId)
        val allKanji = container.kanjiRepository.getAllKanji()
        val relatedKanji = if (radical != null) {
            allKanji.filter { k -> k.radicalReferences.contains(radicalId) }
        } else emptyList()
        value = ScreenState(radical, relatedKanji)
    }

    val title = state.radical?.let { "Kanji using 「${it.character}」" } ?: "Kanji"

    Scaffold(topBar = { KotobaTopBar(title = title, onBack = onBack) }) { padding ->
        when {
            state.radical == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.kanji.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No kanji found for this radical yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Text(
                        text = "${state.kanji.size} kanji",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(state.kanji, key = { it.id }) { kanji ->
                    KanjiRow(kanji = kanji, onClick = { onKanjiClick(kanji.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun KanjiRow(kanji: KanjiEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = kanji.kanji, fontSize = 36.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = kanji.meaning, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (kanji.hiragana.isNotBlank()) {
                Text(text = kanji.hiragana, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }
        if (kanji.jlptLevel.isNotBlank()) {
            Text(text = kanji.jlptLevel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
        }
    }
}
