package com.example.personalproject.misc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog
import com.example.personalproject.util.rememberTts

// ── Data ──────────────────────────────────────────────────────────────────────

private data class CounterForm(
    val number: String,
    val japanese: String,
    val romaji: String,
)

private data class CounterInfo(
    val kanji: String,
    val hiragana: String,
    val romaji: String,
    val meaning: String,
    val usedFor: String,
    val jlptLevel: String,
    val forms: List<CounterForm>,
    val exampleJp: String,
    val exampleEn: String,
    val note: String = "",
)

private data class CounterSection(
    val title: String,
    val emoji: String,
    val entries: List<CounterInfo>,
)

private val counterSections = listOf(

    CounterSection("General Objects", "📦", listOf(
        CounterInfo(
            kanji = "個", hiragana = "こ", romaji = "ko", jlptLevel = "N5",
            meaning = "Small compact objects", usedFor = "apples, eggs, boxes, candy, stamps",
            forms = listOf(
                CounterForm("1", "一個（いっこ）", "ikko"),
                CounterForm("2", "二個（にこ）", "niko"),
                CounterForm("3", "三個（さんこ）", "sanko"),
                CounterForm("6", "六個（ろっこ）", "rokko"),
                CounterForm("10", "十個（じっこ）", "jikko"),
            ),
            exampleJp = "りんごを三個ください。", exampleEn = "Please give me three apples.",
        ),
        CounterInfo(
            kanji = "本", hiragana = "ほん", romaji = "hon / bon / pon", jlptLevel = "N5",
            meaning = "Long cylindrical objects", usedFor = "pens, bottles, trees, rivers, roads, phone calls",
            forms = listOf(
                CounterForm("1", "一本（いっぽん）", "ippon"),
                CounterForm("2", "二本（にほん）", "nihon"),
                CounterForm("3", "三本（さんぼん）", "sanbon"),
                CounterForm("6", "六本（ろっぽん）", "roppon"),
                CounterForm("8", "八本（はっぽん）", "happon"),
            ),
            exampleJp = "ペンを一本貸してください。", exampleEn = "Please lend me one pen.",
            note = "The suffix changes with certain numbers due to rendaku (sequential voicing).",
        ),
        CounterInfo(
            kanji = "枚", hiragana = "まい", romaji = "mai", jlptLevel = "N5",
            meaning = "Flat thin objects", usedFor = "paper, stamps, plates, shirts, slices, tickets",
            forms = listOf(
                CounterForm("1", "一枚（いちまい）", "ichimai"),
                CounterForm("2", "二枚（にまい）", "nimai"),
                CounterForm("3", "三枚（さんまい）", "sanmai"),
                CounterForm("10", "十枚（じゅうまい）", "jūmai"),
            ),
            exampleJp = "切手を二枚ください。", exampleEn = "Please give me two stamps.",
        ),
        CounterInfo(
            kanji = "冊", hiragana = "さつ", romaji = "satsu", jlptLevel = "N4",
            meaning = "Bound volumes", usedFor = "books, magazines, notebooks, manga",
            forms = listOf(
                CounterForm("1", "一冊（いっさつ）", "issatsu"),
                CounterForm("2", "二冊（にさつ）", "nisatsu"),
                CounterForm("3", "三冊（さんさつ）", "sansatsu"),
                CounterForm("8", "八冊（はっさつ）", "hassatsu"),
            ),
            exampleJp = "本を二冊読みました。", exampleEn = "I read two books.",
        ),
        CounterInfo(
            kanji = "台", hiragana = "だい", romaji = "dai", jlptLevel = "N4",
            meaning = "Machines & vehicles", usedFor = "cars, computers, TVs, washing machines",
            forms = listOf(
                CounterForm("1", "一台（いちだい）", "ichidai"),
                CounterForm("2", "二台（にだい）", "nidai"),
                CounterForm("3", "三台（さんだい）", "sandai"),
                CounterForm("10", "十台（じゅうだい）", "jūdai"),
            ),
            exampleJp = "車が三台あります。", exampleEn = "There are three cars.",
        ),
    )),

    CounterSection("People & Animals", "🐾", listOf(
        CounterInfo(
            kanji = "人", hiragana = "にん／り", romaji = "nin / ri", jlptLevel = "N5",
            meaning = "People", usedFor = "counting people (1 and 2 are irregular)",
            forms = listOf(
                CounterForm("1", "一人（ひとり）", "hitori  ← irregular"),
                CounterForm("2", "二人（ふたり）", "futari  ← irregular"),
                CounterForm("3", "三人（さんにん）", "sannin"),
                CounterForm("4", "四人（よにん）", "yonin"),
                CounterForm("10", "十人（じゅうにん）", "jūnin"),
            ),
            exampleJp = "三人でご飯を食べました。", exampleEn = "Three people ate together.",
            note = "一人 (hitori) and 二人 (futari) are native Japanese forms — always use them.",
        ),
        CounterInfo(
            kanji = "匹", hiragana = "ひき", romaji = "hiki / piki / biki", jlptLevel = "N4",
            meaning = "Small animals", usedFor = "dogs, cats, fish, insects, small animals",
            forms = listOf(
                CounterForm("1", "一匹（いっぴき）", "ippiki"),
                CounterForm("2", "二匹（にひき）", "nihiki"),
                CounterForm("3", "三匹（さんびき）", "sanbiki"),
                CounterForm("6", "六匹（ろっぴき）", "roppiki"),
                CounterForm("8", "八匹（はっぴき）", "happiki"),
            ),
            exampleJp = "猫が二匹います。", exampleEn = "There are two cats.",
        ),
        CounterInfo(
            kanji = "頭", hiragana = "とう", romaji = "tō", jlptLevel = "N4",
            meaning = "Large animals", usedFor = "cows, horses, elephants, whales",
            forms = listOf(
                CounterForm("1", "一頭（いっとう）", "ittō"),
                CounterForm("2", "二頭（にとう）", "nitō"),
                CounterForm("3", "三頭（さんとう）", "santō"),
                CounterForm("10", "十頭（じゅっとう）", "juttō"),
            ),
            exampleJp = "馬が三頭います。", exampleEn = "There are three horses.",
        ),
        CounterInfo(
            kanji = "羽", hiragana = "わ", romaji = "wa / ba / ha", jlptLevel = "N4",
            meaning = "Birds & rabbits", usedFor = "birds, rabbits (historically counted like birds)",
            forms = listOf(
                CounterForm("1", "一羽（いちわ）", "ichiwa"),
                CounterForm("2", "二羽（にわ）", "niwa"),
                CounterForm("3", "三羽（さんわ）", "sanwa"),
                CounterForm("6", "六羽（ろくわ）", "rokuwa"),
            ),
            exampleJp = "鳥が五羽います。", exampleEn = "There are five birds.",
        ),
    )),

    CounterSection("Cups & Servings", "🍵", listOf(
        CounterInfo(
            kanji = "杯", hiragana = "はい", romaji = "hai / pai / bai", jlptLevel = "N5",
            meaning = "Cups, glasses, bowls", usedFor = "coffee, tea, soup, ramen, sake",
            forms = listOf(
                CounterForm("1", "一杯（いっぱい）", "ippai"),
                CounterForm("2", "二杯（にはい）", "nihai"),
                CounterForm("3", "三杯（さんばい）", "sanbai"),
                CounterForm("6", "六杯（ろっぱい）", "roppai"),
                CounterForm("8", "八杯（はっぱい）", "happai"),
            ),
            exampleJp = "コーヒーを一杯飲みました。", exampleEn = "I drank one cup of coffee.",
            note = "一杯 (ippai) also means 'full' or 'a lot' in everyday speech.",
        ),
        CounterInfo(
            kanji = "皿", hiragana = "さら", romaji = "sara", jlptLevel = "N4",
            meaning = "Dishes, plates", usedFor = "gyoza, sashimi, or anything served on a plate",
            forms = listOf(
                CounterForm("1", "一皿（ひとさら）", "hitosara"),
                CounterForm("2", "二皿（ふたさら）", "futasara"),
                CounterForm("3", "三皿（みさら）", "misara"),
            ),
            exampleJp = "餃子を二皿ください。", exampleEn = "Please give me two plates of gyoza.",
        ),
    )),

    CounterSection("Time", "⏱", listOf(
        CounterInfo(
            kanji = "時", hiragana = "じ", romaji = "ji", jlptLevel = "N5",
            meaning = "O'clock (hours of the day)", usedFor = "stating what time it is",
            forms = listOf(
                CounterForm("1", "一時（いちじ）", "ichiji"),
                CounterForm("3", "三時（さんじ）", "sanji"),
                CounterForm("7", "七時（しちじ）", "shichiji"),
                CounterForm("12", "十二時（じゅうにじ）", "jūniji"),
            ),
            exampleJp = "三時に会いましょう。", exampleEn = "Let's meet at three o'clock.",
        ),
        CounterInfo(
            kanji = "分", hiragana = "ふん／ぷん", romaji = "fun / pun", jlptLevel = "N5",
            meaning = "Minutes", usedFor = "stating minutes, duration",
            forms = listOf(
                CounterForm("1", "一分（いっぷん）", "ippun"),
                CounterForm("2", "二分（にふん）", "nifun"),
                CounterForm("3", "三分（さんぷん）", "sanpun"),
                CounterForm("6", "六分（ろっぷん）", "roppun"),
                CounterForm("10", "十分（じゅっぷん）", "juppun"),
            ),
            exampleJp = "五分待ってください。", exampleEn = "Please wait five minutes.",
            note = "Pronunciation alternates between ふん and ぷん — 1, 3, 6, 8, 10 use ぷん.",
        ),
        CounterInfo(
            kanji = "日", hiragana = "にち／か", romaji = "nichi / ka", jlptLevel = "N5",
            meaning = "Days (of the month / duration)", usedFor = "days of the month and duration in days",
            forms = listOf(
                CounterForm("1", "一日（ついたち）", "tsuitachi  ← irregular: 1st of month"),
                CounterForm("2", "二日（ふつか）", "futsuka"),
                CounterForm("3", "三日（みっか）", "mikka"),
                CounterForm("14", "十四日（じゅうよっか）", "jūyokka"),
                CounterForm("20", "二十日（はつか）", "hatsuka  ← irregular"),
            ),
            exampleJp = "三日間旅行しました。", exampleEn = "I traveled for three days.",
            note = "Days 1–10 and 14, 20, 24 have special native Japanese readings.",
        ),
        CounterInfo(
            kanji = "週間", hiragana = "しゅうかん", romaji = "shūkan", jlptLevel = "N5",
            meaning = "Weeks", usedFor = "duration in weeks",
            forms = listOf(
                CounterForm("1", "一週間（いっしゅうかん）", "isshūkan"),
                CounterForm("2", "二週間（にしゅうかん）", "nishūkan"),
                CounterForm("3", "三週間（さんしゅうかん）", "sanshūkan"),
            ),
            exampleJp = "二週間日本に滞在します。", exampleEn = "I will stay in Japan for two weeks.",
        ),
        CounterInfo(
            kanji = "ヶ月", hiragana = "かげつ", romaji = "kagetsu", jlptLevel = "N5",
            meaning = "Months (duration)", usedFor = "how many months something lasts",
            forms = listOf(
                CounterForm("1", "一ヶ月（いっかげつ）", "ikkagetsu"),
                CounterForm("2", "二ヶ月（にかげつ）", "nikagetsu"),
                CounterForm("6", "六ヶ月（ろっかげつ）", "rokkagetsu"),
                CounterForm("12", "十二ヶ月（じゅうにかげつ）", "jūnikagetsu"),
            ),
            exampleJp = "六ヶ月間日本語を勉強しています。", exampleEn = "I have been studying Japanese for six months.",
        ),
        CounterInfo(
            kanji = "歳", hiragana = "さい", romaji = "sai", jlptLevel = "N5",
            meaning = "Years old (age)", usedFor = "stating a person's or animal's age",
            forms = listOf(
                CounterForm("1", "一歳（いっさい）", "issai"),
                CounterForm("2", "二歳（にさい）", "nisai"),
                CounterForm("10", "十歳（じゅっさい）", "jussai"),
                CounterForm("20", "二十歳（はたち）", "hatachi  ← irregular"),
            ),
            exampleJp = "私は二十歳です。", exampleEn = "I am twenty years old.",
            note = "20 years old — 二十歳 — is read はたち (hatachi), a special form.",
        ),
    )),

    CounterSection("Ordinal & Sequential", "🔢", listOf(
        CounterInfo(
            kanji = "回", hiragana = "かい", romaji = "kai", jlptLevel = "N4",
            meaning = "Times / occurrences", usedFor = "how many times something happens",
            forms = listOf(
                CounterForm("1", "一回（いっかい）", "ikkai"),
                CounterForm("2", "二回（にかい）", "nikai"),
                CounterForm("3", "三回（さんかい）", "sankai"),
                CounterForm("10", "十回（じっかい）", "jikkai"),
            ),
            exampleJp = "一日三回薬を飲んでください。", exampleEn = "Please take medicine three times a day.",
        ),
        CounterInfo(
            kanji = "階", hiragana = "かい", romaji = "kai", jlptLevel = "N4",
            meaning = "Floors of a building", usedFor = "which floor; how many floors",
            forms = listOf(
                CounterForm("1", "一階（いっかい）", "ikkai"),
                CounterForm("2", "二階（にかい）", "nikai"),
                CounterForm("3", "三階（さんかい）", "sankai"),
                CounterForm("8", "八階（はっかい）", "hakkai"),
            ),
            exampleJp = "図書館は三階にあります。", exampleEn = "The library is on the third floor.",
            note = "回 (occurrences) and 階 (floors) sound identical — context distinguishes them.",
        ),
        CounterInfo(
            kanji = "番", hiragana = "ばん", romaji = "ban", jlptLevel = "N4",
            meaning = "Number / position in sequence", usedFor = "platform numbers, bus lines, seat numbers",
            forms = listOf(
                CounterForm("1", "一番（いちばん）", "ichiban"),
                CounterForm("2", "二番（にばん）", "niban"),
                CounterForm("3", "三番（さんばん）", "sanban"),
            ),
            exampleJp = "一番ホームへどうぞ。", exampleEn = "Please go to platform number one.",
            note = "一番 also means 'the most / the best' as an adverb: 一番好きです (I like it the most).",
        ),
    )),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun CountersScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }
    val speak = rememberTts()

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("counters")) {
            container.onboardingRepository.markScreenSeen("counters")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Counters",
            description = "Japanese uses special counting words (助数詞) that depend on what is being counted.\n\n" +
                "📦 General Objects — 個, 本, 枚, 冊, 台\n" +
                "🐾 People & Animals — 人, 匹, 頭, 羽\n" +
                "🍵 Cups & Servings — 杯, 皿\n" +
                "⏱ Time — 時, 分, 日, 週間, ヶ月, 歳\n" +
                "🔢 Ordinal — 回, 階, 番\n\n" +
                "Tap any counter card to see its key forms, pronunciation, and an example sentence.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Counters",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        var expandedKey by remember { mutableStateOf<String?>(null) }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            counterSections.forEach { section ->
                item {
                    SectionHeader(emoji = section.emoji, title = section.title)
                }
                items(section.entries, key = { it.kanji }) { entry ->
                    CounterCard(
                        entry = entry,
                        expanded = expandedKey == entry.kanji,
                        speak = speak,
                        onToggle = {
                            expandedKey = if (expandedKey == entry.kanji) null else entry.kanji
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp,
        )
    }
}

// ── Counter card ──────────────────────────────────────────────────────────────

@Composable
private fun CounterCard(
    entry: CounterInfo,
    expanded: Boolean,
    speak: (String) -> Unit,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onToggle),
    ) {
        // ── Collapsed header ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Big kanji + TTS
            Column(
                modifier = Modifier.width(56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = entry.kanji,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(
                    onClick = { speak(entry.kanji) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Pronounce",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = entry.hiragana,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = entry.jlptLevel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.usedFor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }

            // Expand arrow
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }

        // ── Expanded content ─────────────────────────────────────────────────
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                // Forms table
                Text(
                    text = "Key Forms",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    entry.forms.forEach { form ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = form.number,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.width(24.dp),
                            )
                            Text(
                                text = form.japanese,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { speak(form.japanese) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "Pronounce",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                )
                            }
                            Text(
                                text = form.romaji,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                // Note
                if (entry.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "💡 ${entry.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }

                // Example
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Example",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = entry.exampleJp,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            IconButton(
                                onClick = { speak(entry.exampleJp) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "Pronounce example",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                )
                            }
                        }
                        Text(
                            text = entry.exampleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic,
                        )
                    }
                }
            }
        }
    }
}
