package com.example.personalproject.data.repository

import com.example.personalproject.data.model.GrammarExample
import com.example.personalproject.data.model.LearningModule
import com.example.personalproject.data.model.Lesson
import com.example.personalproject.data.model.LessonSection
import com.example.personalproject.data.model.ModuleCategory
import com.example.personalproject.data.model.VocabularyWord

/**
 * Provides learning module data.
 *
 * To add a new module:
 *   1. Create a [LearningModule] entry in [allModules].
 *   2. Define its [Lesson] list with [LessonSection]s:
 *        - LessonSection.Text        — explanatory paragraphs
 *        - LessonSection.GrammarRule — pattern + examples
 *        - LessonSection.VocabList   — embedded vocabulary items
 *   3. Rebuild — no other changes required.
 */
class ModuleRepository {

    fun getAllModules(): List<LearningModule> = allModules

    fun getModuleById(id: String): LearningModule? = allModules.find { it.id == id }

    fun getModulesByCategory(category: ModuleCategory): List<LearningModule> =
        allModules.filter { it.category == category }

    // ─────────────────────────────────────────────────────────────────────────
    // Module definitions — edit / add here
    // ─────────────────────────────────────────────────────────────────────────

    private val allModules: List<LearningModule> = listOf(

        // ── PHRASES ──────────────────────────────────────────────────────────
        LearningModule(
            id = "phrases_greetings",
            title = "Basic Greetings",
            description = "Essential greetings and polite expressions for every situation.",
            category = ModuleCategory.PHRASES,
            iconEmoji = "👋",
            lessons = listOf(
                Lesson(
                    id = "greet_1",
                    title = "Time-Based Greetings",
                    sections = listOf(
                        LessonSection.Text(
                            "Japanese greetings change depending on the time of day.\n\n" +
                                "Unlike English, each period of the day has its own dedicated phrase."
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[Time-of-day greeting]",
                            description = "Use the appropriate greeting based on when you meet someone.",
                            examples = listOf(
                                GrammarExample("おはようございます。", "Ohayou gozaimasu.", "Good morning."),
                                GrammarExample("こんにちは。", "Konnichiwa.", "Good afternoon."),
                                GrammarExample("こんばんは。", "Konbanwa.", "Good evening."),
                            )
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("2", "おはよう", "お早う", "ohayou", "Good morning", "Greeting", "N5"),
                                makeWord("1", "こんにちは", "今日は", "konnichiwa", "Good afternoon", "Greeting", "N5"),
                                makeWord("3", "こんばんは", "今晩は", "konbanwa", "Good evening", "Greeting", "N5"),
                            )
                        ),
                    )
                ),
                Lesson(
                    id = "greet_2",
                    title = "Farewells & Thank-yous",
                    sections = listOf(
                        LessonSection.Text(
                            "Learning to say goodbye and express gratitude is essential " +
                                "for any conversation. Japanese has both formal and casual forms."
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("4", "さようなら", "さようなら", "sayounara", "Goodbye", "Expression", "N5"),
                                makeWord("5", "ありがとう", "有難う", "arigatou", "Thank you", "Expression", "N5"),
                                makeWord("6", "すみません", "済みません", "sumimasen", "Excuse me / Sorry", "Expression", "N5"),
                            )
                        ),
                    )
                ),
            )
        ),

        // ── VERBS ─────────────────────────────────────────────────────────────
        LearningModule(
            id = "verbs_existence",
            title = "Existence Verbs",
            description = "Learn いる and ある — the two verbs for expressing existence.",
            category = ModuleCategory.VERBS,
            iconEmoji = "🔵",
            lessons = listOf(
                Lesson(
                    id = "exist_1",
                    title = "いる vs ある",
                    sections = listOf(
                        LessonSection.Text(
                            "Japanese uses two different verbs for 'to exist' or 'to have':\n\n" +
                                "• いる (iru) — for animate beings (people, animals)\n" +
                                "• ある (aru) — for inanimate objects and plants\n\n" +
                                "This distinction is fundamental and used constantly."
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[place] に [subject] が います／あります",
                            description = "Describes where something or someone exists.",
                            examples = listOf(
                                GrammarExample("教室に学生がいます。", "Kyoushitsu ni gakusei ga imasu.", "There are students in the classroom."),
                                GrammarExample("机の上に本があります。", "Tsukue no ue ni hon ga arimasu.", "There is a book on the desk."),
                                GrammarExample("公園に犬がいます。", "Kouen ni inu ga imasu.", "There is a dog in the park."),
                            )
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("17", "いる", "居る", "iru", "To exist (animate)", "Verb (る)", "N5"),
                                makeWord("18", "ある", "有る", "aru", "To exist (inanimate)", "Verb (う)", "N5"),
                            )
                        ),
                    )
                ),
            )
        ),

        LearningModule(
            id = "verbs_basic_action",
            title = "Basic Action Verbs",
            description = "Core action verbs for daily activities.",
            category = ModuleCategory.VERBS,
            iconEmoji = "⚡",
            lessons = listOf(
                Lesson(
                    id = "action_1",
                    title = "Eating, Drinking & Going",
                    sections = listOf(
                        LessonSection.Text(
                            "These three verbs cover the most common daily actions. " +
                                "Notice that たべる is a Group 2 (る) verb while のむ and いく are Group 1 (う) verbs — " +
                                "this affects how they conjugate."
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("10", "たべる", "食べる", "taberu", "To eat", "Verb (る)", "N5"),
                                makeWord("11", "のむ", "飲む", "nomu", "To drink", "Verb (う)", "N5"),
                                makeWord("14", "いく", "行く", "iku", "To go", "Verb (う)", "N5"),
                            )
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[object] を [verb in ます form]",
                            description = "The を particle marks the direct object of an action verb.",
                            examples = listOf(
                                GrammarExample("ご飯を食べます。", "Gohan wo tabemasu.", "I eat rice."),
                                GrammarExample("水を飲みます。", "Mizu wo nomimasu.", "I drink water."),
                                GrammarExample("学校に行きます。", "Gakkou ni ikimasu.", "I go to school."),
                            )
                        ),
                    )
                ),
            )
        ),

        // ── NOUNS ─────────────────────────────────────────────────────────────
        LearningModule(
            id = "nouns_everyday",
            title = "Everyday Nouns",
            description = "Common objects and concepts you'll encounter daily.",
            category = ModuleCategory.NOUNS,
            iconEmoji = "📦",
            lessons = listOf(
                Lesson(
                    id = "noun_1",
                    title = "School & Study",
                    sections = listOf(
                        LessonSection.Text(
                            "Nouns in Japanese do not change form for number (plural/singular) " +
                                "or gender. Context and counters are used to indicate quantity."
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("19", "ほん", "本", "hon", "Book", "Noun", "N5"),
                                makeWord("20", "がっこう", "学校", "gakkou", "School", "Noun", "N5"),
                                makeWord("21", "せんせい", "先生", "sensei", "Teacher", "Noun", "N5"),
                                makeWord("22", "がくせい", "学生", "gakusei", "Student", "Noun", "N5"),
                            )
                        ),
                    )
                ),
                Lesson(
                    id = "noun_2",
                    title = "Food & Daily Items",
                    sections = listOf(
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("23", "みず", "水", "mizu", "Water", "Noun", "N5"),
                                makeWord("24", "たべもの", "食べ物", "tabemono", "Food", "Noun", "N5"),
                                makeWord("25", "おかね", "お金", "okane", "Money", "Noun", "N5"),
                            )
                        ),
                    )
                ),
            )
        ),

        // ── ADJECTIVES ────────────────────────────────────────────────────────
        LearningModule(
            id = "adj_i_adjectives",
            title = "い-Adjectives",
            description = "Adjectives ending in い that conjugate directly.",
            category = ModuleCategory.ADJECTIVES,
            iconEmoji = "🎨",
            lessons = listOf(
                Lesson(
                    id = "iadj_1",
                    title = "Basic い-Adjectives",
                    sections = listOf(
                        LessonSection.Text(
                            "い-adjectives end in い and conjugate like verbs.\n\n" +
                                "Present affirmative: [adj] + です\n" +
                                "Negative: drop い, add くない + です\n" +
                                "Example: 大きい → 大きくない (not big)"
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[noun] は [い-adjective] です",
                            description = "Standard predicate sentence using an い-adjective.",
                            examples = listOf(
                                GrammarExample("この犬は大きいです。", "Kono inu wa ookii desu.", "This dog is big."),
                                GrammarExample("あのビルは高いです。", "Ano biru wa takai desu.", "That building is tall."),
                                GrammarExample("このラーメンはおいしいです。", "Kono raamen wa oishii desu.", "This ramen is delicious."),
                            )
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("26", "おおきい", "大きい", "ookii", "Big", "い-Adjective", "N5"),
                                makeWord("27", "ちいさい", "小さい", "chiisai", "Small", "い-Adjective", "N5"),
                                makeWord("28", "あたらしい", "新しい", "atarashii", "New", "い-Adjective", "N5"),
                                makeWord("29", "ふるい", "古い", "furui", "Old (objects)", "い-Adjective", "N5"),
                                makeWord("30", "たかい", "高い", "takai", "Tall / Expensive", "い-Adjective", "N5"),
                                makeWord("31", "やすい", "安い", "yasui", "Cheap", "い-Adjective", "N5"),
                                makeWord("32", "おいしい", "美味しい", "oishii", "Delicious", "い-Adjective", "N5"),
                            )
                        ),
                    )
                ),
            )
        ),

        // ── PARTICLES ─────────────────────────────────────────────────────────
        LearningModule(
            id = "particles_core",
            title = "Core Particles",
            description = "Master は, が, を, に, で and other essential particles.",
            category = ModuleCategory.PARTICLES,
            iconEmoji = "🔗",
            lessons = listOf(
                Lesson(
                    id = "particle_1",
                    title = "は (Topic) vs が (Subject)",
                    sections = listOf(
                        LessonSection.Text(
                            "は marks the *topic* of the sentence — what you're talking about.\n" +
                                "が marks the grammatical *subject* — who/what performs the action.\n\n" +
                                "They often overlap but have distinct nuances:\n" +
                                "• 私は学生です — 'As for me, I am a student.' (topic)\n" +
                                "• 猫がいます — 'There is a cat.' (subject introducing new info)"
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[topic] は [comment]",
                            description = "は introduces the topic of a sentence.",
                            examples = listOf(
                                GrammarExample("私は学生です。", "Watashi wa gakusei desu.", "I am a student."),
                                GrammarExample("これは本です。", "Kore wa hon desu.", "This is a book."),
                            )
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[subject] が [verb/adjective]",
                            description = "が marks the subject, often for new or specific information.",
                            examples = listOf(
                                GrammarExample("猫がいます。", "Neko ga imasu.", "There is a cat."),
                                GrammarExample("雨が降っています。", "Ame ga futte imasu.", "It is raining."),
                            )
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("33", "は", "は", "wa", "Topic marker", "Particle", "N5"),
                                makeWord("34", "が", "が", "ga", "Subject marker", "Particle", "N5"),
                            )
                        ),
                    )
                ),
                Lesson(
                    id = "particle_2",
                    title = "を, に, で",
                    sections = listOf(
                        LessonSection.Text(
                            "Three more essential particles:\n\n" +
                                "• を (wo) — marks the direct object of an action\n" +
                                "• に (ni) — direction, destination, or point-in-time\n" +
                                "• で (de) — location where an action takes place, or means/method"
                        ),
                        LessonSection.VocabList(
                            words = listOf(
                                makeWord("35", "を", "を", "wo", "Object marker", "Particle", "N5"),
                                makeWord("36", "に", "に", "ni", "To / At / In", "Particle", "N5"),
                                makeWord("37", "で", "で", "de", "At / By means of", "Particle", "N5"),
                            )
                        ),
                    )
                ),
            )
        ),

        // ── GRAMMAR ───────────────────────────────────────────────────────────
        LearningModule(
            id = "grammar_sentence_structure",
            title = "Basic Sentence Structure",
            description = "SOV word order and the core pattern of Japanese sentences.",
            category = ModuleCategory.GRAMMAR,
            iconEmoji = "📐",
            lessons = listOf(
                Lesson(
                    id = "grammar_1",
                    title = "Subject–Object–Verb",
                    sections = listOf(
                        LessonSection.Text(
                            "Japanese follows Subject–Object–Verb (SOV) order, the opposite of English.\n\n" +
                                "English: I [S] eat [V] sushi [O]\n" +
                                "Japanese: 私 [S] は 寿司 [O] を 食べます [V]\n\n" +
                                "The verb always comes last. Particles mark the grammatical role " +
                                "of each word so the order can be flexible, but verb-last is the norm."
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[Subject] は [Object] を [Verb-ます]",
                            description = "The standard affirmative present-tense sentence pattern.",
                            examples = listOf(
                                GrammarExample("私は本を読みます。", "Watashi wa hon wo yomimasu.", "I read a book."),
                                GrammarExample("田中さんは音楽を聴きます。", "Tanaka-san wa ongaku wo kikimasu.", "Tanaka listens to music."),
                                GrammarExample("子供はご飯を食べます。", "Kodomo wa gohan wo tabemasu.", "The child eats rice."),
                            )
                        ),
                    )
                ),
                Lesson(
                    id = "grammar_2",
                    title = "Negative & Question Forms",
                    sections = listOf(
                        LessonSection.Text(
                            "To make a negative sentence, change ます to ません.\n" +
                                "To ask a question, add か at the end — no word order change needed.\n\n" +
                                "Affirmative: 食べます (tabemasu) — eats\n" +
                                "Negative:    食べません (tabemasen) — does not eat\n" +
                                "Question:    食べますか？ (tabemasu ka?) — Do you eat?"
                        ),
                        LessonSection.GrammarRule(
                            pattern = "[Verb-ます] → [Verb-ません] / [Verb-ますか？]",
                            description = "Polite negation and question formation.",
                            examples = listOf(
                                GrammarExample("水を飲みません。", "Mizu wo nomimasen.", "I don't drink water."),
                                GrammarExample("学校に行きますか？", "Gakkou ni ikimasu ka?", "Do you go to school?"),
                                GrammarExample("これは本ですか？", "Kore wa hon desu ka?", "Is this a book?"),
                            )
                        ),
                    )
                ),
            )
        ),
    )

    /** Helper to construct a minimal [VocabularyWord] for inline lesson vocab. */
    private fun makeWord(
        id: String, japanese: String, kanji: String, romaji: String,
        english: String, pos: String, jlpt: String,
    ) = VocabularyWord(
        id = id, japanese = japanese, hiragana = kanji, romaji = romaji,
        english = english, partOfSpeech = pos, jlptLevel = jlpt,
        kanjiReferences = emptyList(), category = "", exampleJapanese = "",
        exampleEnglish = "", notes = "", partOfSpeechReferences = emptyList(),
        frequency = "", pitchAccent = "",
    )
}
