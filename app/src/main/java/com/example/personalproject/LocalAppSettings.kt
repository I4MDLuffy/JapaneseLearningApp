package app.kotori.japanese

import androidx.compose.runtime.compositionLocalOf
import app.kotori.japanese.data.model.AppSettings

val LocalAppSettings = compositionLocalOf<AppSettings> { AppSettings() }
