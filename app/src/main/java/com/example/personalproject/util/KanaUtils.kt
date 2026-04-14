package app.kotori.japanese.util

/** Two-character yōon combinations (hiragana). */
private val KANA_TWO = mapOf(
    "きゃ" to "kya", "きゅ" to "kyu", "きょ" to "kyo",
    "しゃ" to "sha", "しゅ" to "shu", "しょ" to "sho",
    "ちゃ" to "cha", "ちゅ" to "chu", "ちょ" to "cho",
    "にゃ" to "nya", "にゅ" to "nyu", "にょ" to "nyo",
    "ひゃ" to "hya", "ひゅ" to "hyu", "ひょ" to "hyo",
    "みゃ" to "mya", "みゅ" to "myu", "みょ" to "myo",
    "りゃ" to "rya", "りゅ" to "ryu", "りょ" to "ryo",
    "ぎゃ" to "gya", "ぎゅ" to "gyu", "ぎょ" to "gyo",
    "じゃ" to "ja",  "じゅ" to "ju",  "じょ" to "jo",
    "ぢゃ" to "ja",  "ぢゅ" to "ju",  "ぢょ" to "jo",
    "びゃ" to "bya", "びゅ" to "byu", "びょ" to "byo",
    "ぴゃ" to "pya", "ぴゅ" to "pyu", "ぴょ" to "pyo",
)

/** Single-character hiragana mappings (Hepburn). */
private val KANA_ONE = mapOf(
    'あ' to "a",   'い' to "i",   'う' to "u",   'え' to "e",   'お' to "o",
    'か' to "ka",  'き' to "ki",  'く' to "ku",  'け' to "ke",  'こ' to "ko",
    'さ' to "sa",  'し' to "shi", 'す' to "su",  'せ' to "se",  'そ' to "so",
    'た' to "ta",  'ち' to "chi", 'つ' to "tsu", 'て' to "te",  'と' to "to",
    'な' to "na",  'に' to "ni",  'ぬ' to "nu",  'ね' to "ne",  'の' to "no",
    'は' to "ha",  'ひ' to "hi",  'ふ' to "fu",  'へ' to "he",  'ほ' to "ho",
    'ま' to "ma",  'み' to "mi",  'む' to "mu",  'め' to "me",  'も' to "mo",
    'や' to "ya",  'ゆ' to "yu",  'よ' to "yo",
    'ら' to "ra",  'り' to "ri",  'る' to "ru",  'れ' to "re",  'ろ' to "ro",
    'わ' to "wa",  'ゐ' to "i",   'ゑ' to "e",   'を' to "o",
    // voiced
    'が' to "ga",  'ぎ' to "gi",  'ぐ' to "gu",  'げ' to "ge",  'ご' to "go",
    'ざ' to "za",  'じ' to "ji",  'ず' to "zu",  'ぜ' to "ze",  'ぞ' to "zo",
    'だ' to "da",  'ぢ' to "ji",  'づ' to "zu",  'で' to "de",  'ど' to "do",
    'ば' to "ba",  'び' to "bi",  'ぶ' to "bu",  'べ' to "be",  'ぼ' to "bo",
    // semi-voiced
    'ぱ' to "pa",  'ぴ' to "pi",  'ぷ' to "pu",  'ぺ' to "pe",  'ぽ' to "po",
    // small kana (standalone)
    'ぁ' to "a",   'ぃ' to "i",   'ぅ' to "u",   'ぇ' to "e",   'ぉ' to "o",
    'ゃ' to "ya",  'ゅ' to "yu",  'ょ' to "yo",  'ゎ' to "wa",
)

/**
 * Returns true if [text] contains any hiragana or katakana characters.
 * Used to decide whether to show a romaji line in the UI.
 */
fun containsKana(text: String): Boolean =
    text.any { it in '\u3040'..'\u30FF' }

/**
 * Converts a string of kana (hiragana or katakana) to Hepburn romaji.
 * Non-kana characters (kanji, punctuation, ASCII) are passed through unchanged.
 *
 * Handles: yōon, double consonants (っ/ッ), nasal ん/ン, long vowel ー.
 */
fun kanaToRomaji(text: String): String {
    if (text.isBlank()) return text
    // Convert katakana (U+30A1–U+30F6) → hiragana by subtracting 0x60.
    // ー (U+30FC) is outside this range and is handled explicitly below.
    val hira = buildString {
        for (c in text) {
            append(if (c in '\u30A1'..'\u30F6') (c.code - 0x60).toChar() else c)
        }
    }
    val sb = StringBuilder()
    var i = 0
    while (i < hira.length) {
        // Try 2-char yōon first
        if (i + 1 < hira.length) {
            val r = KANA_TWO[hira.substring(i, i + 2)]
            if (r != null) { sb.append(r); i += 2; continue }
        }
        when (val c = hira[i]) {
            'っ' -> {
                // Double the first consonant of the following mora
                val nextTwo = if (i + 2 < hira.length) hira.substring(i + 1, i + 3) else null
                val nextOne = if (i + 1 < hira.length) hira[i + 1] else '\u0000'
                val nextRomaji = (if (nextTwo != null) KANA_TWO[nextTwo] else null) ?: KANA_ONE[nextOne]
                if (!nextRomaji.isNullOrEmpty()) sb.append(nextRomaji[0])
            }
            'ん' -> {
                // Use 'm' before bilabial/labial sounds, 'n' elsewhere
                val next = if (i + 1 < hira.length) hira[i + 1] else '\u0000'
                sb.append(if (next in "ばびぶべぼまみむめもぱぴぷぺぽ") 'm' else 'n')
            }
            'ー' -> {
                // Long vowel: repeat the previous vowel
                if (sb.isNotEmpty() && sb.last().lowercaseChar() in "aeiou") sb.append(sb.last())
            }
            else -> sb.append(KANA_ONE[c] ?: c.toString())
        }
        i++
    }
    return sb.toString()
}
