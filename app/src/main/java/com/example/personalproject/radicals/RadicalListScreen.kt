package com.example.personalproject.radicals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.RadicalEntry
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun RadicalListScreen(
    onBack: () -> Unit,
    onRadicalClick: (id: String, allIds: String) -> Unit,
    onStudyGroup: (groupId: String) -> Unit,
    onStudyAll: () -> Unit,
) {
    val container = LocalAppContainer.current
    val radicals by produceState<List<RadicalEntry>>(emptyList()) {
        value = container.radicalRepository.getAllRadicals()
    }

    Scaffold(
        topBar = { KotobaTopBar(title = "Radicals", onBack = onBack) }
    ) { padding ->
        if (radicals.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val byStroke = radicals.groupBy { it.strokeCount }.entries.sortedBy { it.key }
        val allIds = radicals.sortedBy { it.strokeCount }.joinToString(",") { it.id }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onStudyAll, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Text("Study All", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            byStroke.forEach { (strokeCount, group) ->
                item {
                    StrokeGroupHeader(
                        strokeCount = strokeCount,
                        count = group.size,
                        onStudy = { onStudyGroup(strokeCount.toString()) },
                    )
                }
                item {
                    RadicalGroupGrid(
                        radicals = group,
                        onRadicalClick = { id -> onRadicalClick(id, allIds) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StrokeGroupHeader(strokeCount: Int, count: Int, onStudy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "$strokeCount stroke${if (strokeCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "$count radical${if (count != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }
            OutlinedButton(onClick = onStudy) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                Text("Play", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun RadicalGroupGrid(radicals: List<RadicalEntry>, onRadicalClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        radicals.chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { radical ->
                    RadicalTile(
                        radical = radical,
                        modifier = Modifier.weight(1f),
                        onClick = { onRadicalClick(radical.id) },
                    )
                }
                repeat(5 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RadicalTile(radical: RadicalEntry, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = radical.character,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = radical.meaning,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
