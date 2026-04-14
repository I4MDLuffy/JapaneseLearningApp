package app.kotori.japanese.data.csv

import app.kotori.japanese.data.model.AdjectiveEntry
import app.kotori.japanese.data.model.DialogueEntry
import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.data.model.KanaCharacter
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.model.MiscEntry
import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.data.model.PhraseEntry
import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

object CsvParser {

    // ── Vocabulary ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseVocabulary(fileName: String): List<VocabularyWord> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            VocabularyWord(
                id = f["id"] ?: return@mapNotNull null,
                japanese = f["japanese"].orEmpty(),
                hiragana = f["hiragana"].orEmpty(),
                kanjiReferences = splitList(f["kanji_references"]),
                romaji = f["romaji"].orEmpty(),
                english = f["english"].orEmpty(),
                category = f["category"].orEmpty(),
                jlptLevel = f["jlpt_level"].orEmpty(),
                exampleJapanese = f["example_japanese"].orEmpty(),
                exampleEnglish = f["example_english"].orEmpty(),
                notes = f["notes"].orEmpty(),
                partOfSpeech = f["part_of_speech"].orEmpty(),
                partOfSpeechReferences = splitList(f["part_of_speech_references"]),
                frequency = f["frequency"].orEmpty(),
                pitchAccent = f["pitch_accent"].orEmpty(),
            )
        }
    }

    // ── Kanji ─────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseKanji(fileName: String): List<KanjiEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            KanjiEntry(
                id = f["id"] ?: return@mapNotNull null,
                kanji = f["kanji"].orEmpty(),
                meaning = f["meaning"].orEmpty(),
                hiragana = f["hiragana"].orEmpty(),
                onYomi = splitList(f["on_yomi"]),
                kunYomi = splitList(f["kun_yomi"]),
                vocabReferences = splitList(f["vocab_references"]),
                jlptLevel = f["jlpt_level"].orEmpty(),
                theme = f["theme"].orEmpty(),
                unicode = f["unicode"].orEmpty(),
                strokeOrderImage = f["stroke_order_image"].orEmpty(),
                radicalReferences = splitList(f["radical_references"]),
                strokeCount = f["stroke_count"]?.toIntOrNull() ?: 0,
                gradeLevel = f["grade_level"].orEmpty(),
                componentStructure = f["component_structure"].orEmpty(),
            )
        }
    }

    // ── Verbs ─────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseVerbs(fileName: String): List<VerbEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            VerbEntry(
                id = f["id"] ?: return@mapNotNull null,
                kanji = f["kanji"].orEmpty(),
                kanjiRadical = f["kanji_radical"].orEmpty(),
                dictionaryForm = f["dictionary_form"].orEmpty(),
                romaji = f["romaji"].orEmpty(),
                stem = f["stem"].orEmpty(),
                transitivity = f["transitivity"].orEmpty(),
                hiragana = f["hiragana"].orEmpty(),
                meaning = f["meaning"].orEmpty(),
                verbType = f["verb_type"].orEmpty(),
                theme = f["theme"].orEmpty(),
                presentAffirmative = f["present_affirmative"].orEmpty(),
                presentNegative = f["present_negative"].orEmpty(),
                pastAffirmative = f["past_affirmative"].orEmpty(),
                pastNegative = f["past_negative"].orEmpty(),
                teFormAffirmative = f["te_form_affirmative"].orEmpty(),
                presentShortNegative = f["present_short_negative"].orEmpty(),
                teFormNegativeNaide = f["te_form_negative_naide"].orEmpty(),
                teFormNegativeNakute = f["te_form_negative_nakute"].orEmpty(),
                pastShortAffirmative = f["past_short_affirmative"].orEmpty(),
                pastShortNegative = f["past_short_negative"].orEmpty(),
                tai = f["tai"].orEmpty(),
                volitional = f["volitional"].orEmpty(),
                baFormAffirmative = f["ba_form_affirmative"].orEmpty(),
                baFormNegative = f["ba_form_negative"].orEmpty(),
                potential = f["potential"].orEmpty(),
                causative = f["causative"].orEmpty(),
                passive = f["passive"].orEmpty(),
                causativePassive = f["causative_passive"].orEmpty(),
                grammarReferences = splitList(f["grammar_references"]),
                kanjiReferences = splitList(f["kanji_references"]),
                jlptLevel = f["jlpt_level"].orEmpty(),
            )
        }
    }

    // ── Adjectives ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseAdjectives(fileName: String): List<AdjectiveEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            AdjectiveEntry(
                id = f["id"] ?: return@mapNotNull null,
                kanji = f["kanji"].orEmpty(),
                hiragana = f["hiragana"].orEmpty(),
                romaji = f["romaji"].orEmpty(),
                examplePhraseReferences = splitList(f["example_phrase_references"]),
                meaning = f["meaning"].orEmpty(),
                adjType = f["adjective_type"].orEmpty(),
                theme = f["theme"].orEmpty(),
                pastAffirmative = f["past_affirmative"].orEmpty(),
                pastNegative = f["past_negative"].orEmpty(),
                pastAffirmativeShort = f["past_affirmative_short"].orEmpty(),
                pastNegativeShort = f["past_negative_short"].orEmpty(),
                presentAffirmative = f["present_affirmative"].orEmpty(),
                presentNegative = f["present_negative"].orEmpty(),
                presentNegativeShort = f["present_negative_short"].orEmpty(),
                teFormAffirmative = f["te_form_affirmative"].orEmpty(),
                teFormNegative = f["te_form_negative"].orEmpty(),
                adjNaru = f["adj_naru"].orEmpty(),
                grammarReferences = splitList(f["grammar_references"]),
                kanjiReferences = splitList(f["kanji_references"]),
                jlptLevel = f["jlpt_level"].orEmpty(),
                unlockedAtGrammarId = f["unlocked_at_grammar_id"].orEmpty(),
            )
        }
    }

    // ── Nouns ─────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseNouns(fileName: String): List<NounEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            NounEntry(
                id = f["id"] ?: return@mapNotNull null,
                kanji = f["kanji"].orEmpty(),
                kanjiRadicals = splitList(f["kanji_radicals"]),
                romaji = f["romaji"].orEmpty(),
                pitchAccent = f["pitch_accent"].orEmpty(),
                hiragana = f["hiragana"].orEmpty(),
                exampleReferences = splitList(f["example_references"]),
                alternateReading = f["alternate_reading"].orEmpty(),
                meaning = f["meaning"].orEmpty(),
                theme = f["theme"].orEmpty(),
                radicalReferences = splitList(f["radical_references"]),
                jlptLevel = f["jlpt_level"].orEmpty(),
                unlockedAtGrammarId = f["unlocked_at_grammar_id"].orEmpty(),
            )
        }
    }

    // ── Phrases ───────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parsePhrases(fileName: String): List<PhraseEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            PhraseEntry(
                id = f["id"] ?: return@mapNotNull null,
                phrase = f["phrase"].orEmpty(),
                reading = f["reading"].orEmpty(),
                meaning = f["meaning"].orEmpty(),
                vocabularyReferences = splitList(f["vocabulary_references"]),
                kanjiReferences = splitList(f["kanji_references"]),
                jlptLevel = f["jlpt_level"].orEmpty(),
                category = f["category"].orEmpty(),
                romaji = f["romaji"].orEmpty(),
                grammarReferences = splitList(f["grammar_references"]),
            )
        }
    }

    // ── Grammar ───────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseGrammar(fileName: String): List<GrammarEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            GrammarEntry(
                id = f["id"] ?: return@mapNotNull null,
                lessonNumber = f["lesson_number"]?.toIntOrNull() ?: 0,
                title = f["title"].orEmpty(),
                content = f["content"].orEmpty(),
                exampleOne = f["example_one"].orEmpty(),
                exampleTwo = f["example_two"].orEmpty(),
                supportingContent = f["supporting_content"].orEmpty(),
                jlptLevel = f["jlpt_level"].orEmpty(),
                category = f["category"].orEmpty(),
                relatedGrammarReferences = splitList(f["related_grammar_references"]),
                relatedKanjiReferences = splitList(f["related_kanji_references"]),
                relatedVocabReferences = splitList(f["related_vocab_references"]),
                difficultyOrder = f["difficulty_order"]?.toIntOrNull() ?: 0,
                unlocksContent = f["unlocks_content"].orEmpty(),
            )
        }
    }

    // ── Kana ──────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseKana(fileName: String): List<KanaCharacter> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            KanaCharacter(
                id = f["id"] ?: return@mapNotNull null,
                character = f["character"].orEmpty(),
                romaji = f["romaji"].orEmpty(),
                hiraganaOrKatakana = f["hiragana_or_katakana"].orEmpty(),
                group = f["group"].orEmpty(),
                strokeOrder = f["stroke_order"].orEmpty(),
            )
        }
    }

    // ── Radicals ──────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseRadicals(fileName: String): List<RadicalEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            RadicalEntry(
                id = f["id"] ?: return@mapNotNull null,
                character = f["character"].orEmpty(),
                meaning = f["meaning"].orEmpty(),
                strokeCount = f["stroke_count"]?.toIntOrNull() ?: 0,
                position = f["position"].orEmpty(),
                frequency = f["frequency"].orEmpty(),
                exampleKanji = splitList(f["example_kanji"]),
                kanjiReferences = splitList(f["kanji_references"]),
                variantForms = splitList(f["variant_forms"]),
            )
        }
    }

    // ── Dialogues ─────────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseDialogues(fileName: String): List<DialogueEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            DialogueEntry(
                id = f["id"] ?: return@mapNotNull null,
                japaneseContent = f["japanese_content"].orEmpty(),
                englishContent = f["english_content"].orEmpty(),
                kanjiReferences = splitList(f["kanji_references"]),
                vocabReferences = splitList(f["vocab_references"]),
                reading = f["reading"].orEmpty(),
                romaji = f["romaji"].orEmpty(),
            )
        }
    }

    // ── Miscellaneous ─────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    suspend fun parseMisc(fileName: String): List<MiscEntry> {
        val (_, rows) = readCsv(fileName)
        return rows.mapNotNull { f ->
            MiscEntry(
                id = f["id"] ?: return@mapNotNull null,
                type = f["type"].orEmpty(),
                content = f["content"].orEmpty(),
                kanjiReferences = splitList(f["kanji_references"]),
                vocabReferences = splitList(f["vocab_references"]),
                grammarReferences = splitList(f["grammar_references"]),
            )
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readCsv(fileName: String): Pair<List<String>, List<Map<String, String>>> {
        val text = Res.readBytes("files/$fileName").decodeToString()
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return Pair(emptyList(), emptyList())

        val header = parseLine(lines[0])
        val rows = lines.drop(1).mapNotNull { line ->
            val values = parseLine(line)
            if (values.isEmpty()) return@mapNotNull null
            val padded = values + List((header.size - values.size).coerceAtLeast(0)) { "" }
            header.zip(padded).toMap()
        }
        return Pair(header, rows)
    }

    private fun splitList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else value.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result += current.toString().trim()
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result += current.toString().trim()
        return result
    }
}
