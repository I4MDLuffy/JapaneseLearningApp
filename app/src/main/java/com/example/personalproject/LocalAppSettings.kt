package com.example.personalproject

import androidx.compose.runtime.compositionLocalOf
import com.example.personalproject.data.model.AppSettings

val LocalAppSettings = compositionLocalOf<AppSettings> { AppSettings() }
