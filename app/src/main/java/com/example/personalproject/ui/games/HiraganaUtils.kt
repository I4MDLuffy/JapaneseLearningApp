package app.kotori.japanese.ui.games

internal object HiraganaUtils {

    val POOL: List<String> = listOf(
        "あ", "い", "う", "え", "お",
        "か", "き", "く", "け", "こ",
        "さ", "し", "す", "せ", "そ",
        "た", "ち", "つ", "て", "と",
        "な", "に", "ぬ", "ね", "の",
        "は", "ひ", "ふ", "へ", "ほ",
        "ま", "み", "む", "め", "も",
        "や", "ゆ", "よ",
        "ら", "り", "る", "れ", "ろ",
        "わ", "を", "ん",
        "が", "ぎ", "ぐ", "げ", "ご",
        "ざ", "じ", "ず", "ぜ", "ぞ",
        "だ", "で", "ど",
        "ば", "び", "ぶ", "べ", "ぼ",
    )

    private val TO_ROMAJI: Map<String, String> = mapOf(
        "あ" to "a",   "い" to "i",   "う" to "u",   "え" to "e",   "お" to "o",
        "か" to "ka",  "き" to "ki",  "く" to "ku",  "け" to "ke",  "こ" to "ko",
        "さ" to "sa",  "し" to "shi", "す" to "su",  "せ" to "se",  "そ" to "so",
        "た" to "ta",  "ち" to "chi", "つ" to "tsu", "て" to "te",  "と" to "to",
        "な" to "na",  "に" to "ni",  "ぬ" to "nu",  "ね" to "ne",  "の" to "no",
        "は" to "ha",  "ひ" to "hi",  "ふ" to "fu",  "へ" to "he",  "ほ" to "ho",
        "ま" to "ma",  "み" to "mi",  "む" to "mu",  "め" to "me",  "も" to "mo",
        "や" to "ya",  "ゆ" to "yu",  "よ" to "yo",
        "ら" to "ra",  "り" to "ri",  "る" to "ru",  "れ" to "re",  "ろ" to "ro",
        "わ" to "wa",  "を" to "wo",  "ん" to "n",
        "が" to "ga",  "ぎ" to "gi",  "ぐ" to "gu",  "げ" to "ge",  "ご" to "go",
        "ざ" to "za",  "じ" to "ji",  "ず" to "zu",  "ぜ" to "ze",  "ぞ" to "zo",
        "だ" to "da",  "ぢ" to "ji",  "づ" to "zu",  "で" to "de",  "ど" to "do",
        "ば" to "ba",  "び" to "bi",  "ぶ" to "bu",  "べ" to "be",  "ぼ" to "bo",
        "ぱ" to "pa",  "ぴ" to "pi",  "ぷ" to "pu",  "ぺ" to "pe",  "ぽ" to "po",
        "っ" to "~",   "ゃ" to "ya",  "ゅ" to "yu",  "ょ" to "yo",
        "ぁ" to "a",   "ぃ" to "i",   "ぅ" to "u",   "ぇ" to "e",   "ぉ" to "o",
    )

    fun toRomaji(ch: String): String = TO_ROMAJI[ch] ?: ch

    fun decompose(reading: String): List<String> = reading.map { it.toString() }

    /**
     * Build a grid of [gridSize] tiles for the given hiragana reading.
     * Correct chars are seeded in; remaining slots filled with distractors from POOL.
     */
    fun buildGrid(reading: String, gridSize: Int = 12): List<String> {
        val target = decompose(reading)
        val need = (gridSize - target.size).coerceAtLeast(0)
        val distractors = POOL
            .filter { it !in target.toSet() }
            .shuffled()
            .take(need)
        return (target + distractors).shuffled()
    }
}
