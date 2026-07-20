package com.talos.forge.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

object AppSettings {
    private lateinit var prefs: SharedPreferences

    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> = _isDarkMode

    private val _language = mutableStateOf("es")
    val language: State<String> = _language

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean("dark_mode", false)
        _language.value = prefs.getString("language", "es") ?: "es"
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("language", lang).apply()
    }

    fun toggleLanguage() {
        setLanguage(if (_language.value == "es") "en" else "es")
    }

    val isSpanish: Boolean get() = _language.value == "es"
}
