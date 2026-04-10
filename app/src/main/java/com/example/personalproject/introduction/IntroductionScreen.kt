package com.example.personalproject.introduction

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer

@Composable
fun IntroductionScreen(onGetStarted: () -> Unit) {
    val container = LocalAppContainer.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "言",
                fontSize = 42.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to 言葉 Kotoba",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your complete Japanese learning companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        IntroSection(
            emoji = "📚",
            title = "What is Kotoba?",
            body = "Kotoba (言葉, meaning \"word\" or \"language\") is a structured Japanese learning app covering everything from the hiragana alphabet to advanced grammar. Whether you're a complete beginner or looking to deepen your knowledge, Kotoba has a path for you.",
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        IntroSection(
            emoji = "🗂",
            title = "Structured Learning",
            body = "The left column on the Home screen follows a step-by-step path:\n\n• Basic Characters — learn hiragana and katakana first\n• Beginner (N5) — essential vocabulary and grammar\n• Intermediate (N4–N3) — expand your foundations\n• Advanced (N2–N1) — approach fluency\n• Master — native-level material",
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        IntroSection(
            emoji = "🔍",
            title = "Explore",
            body = "The right column lets you browse by topic at any time:\n\n• Grammar — rules and patterns with examples\n• Conversational — practical everyday phrases\n• Counters — Japanese counting words\n• Term Study — browse kanji, verbs, nouns, and more\n• Dialogues — read real-style conversations",
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        IntroSection(
            emoji = "📱",
            title = "Navigation",
            body = "The bar at the bottom of every screen gives you quick access to four areas:\n\n• Home — return to the main hub\n• Saved — items you've bookmarked for review\n• Games — interactive study games\n• Settings — theme, display, and study preferences",
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        IntroSection(
            emoji = "❓",
            title = "Help button",
            body = "Every screen has a help button (?) in the top-right corner. Tap it any time to see a description of that screen's features and how to use it.",
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                container.onboardingRepository.markIntroSeen()
                onGetStarted()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IntroSection(emoji: String, title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            lineHeight = 22.sp,
        )
    }
}
