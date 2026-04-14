# ── Kotlin ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# ── Kotlin Serialization ──────────────────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-dontwarn kotlinx.serialization.**

# ── Room (KMP 2.7+) ───────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class ** { *; }
-keep @androidx.room.Dao interface ** { *; }
-keepclassmembers class ** {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-dontwarn androidx.room.**

# ── multiplatform-settings ────────────────────────────────────────────────────
-keep class com.russhwolf.settings.** { *; }
-dontwarn com.russhwolf.settings.**

# ── kotlinx-datetime ──────────────────────────────────────────────────────────
-dontwarn kotlinx.datetime.**

# ── Compose ───────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Navigation Compose (serializable routes) ──────────────────────────────────
-keep class app.kotori.japanese.navigation.** { *; }
-keepclassmembers class app.kotori.japanese.navigation.** { *; }

# ── App data models ───────────────────────────────────────────────────────────
-keep class app.kotori.japanese.data.** { *; }

# ── Preserve stack traces in crash reports ────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
