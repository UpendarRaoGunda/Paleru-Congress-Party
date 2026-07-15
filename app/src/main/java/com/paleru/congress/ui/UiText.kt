package com.paleru.congress.ui

import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.LocalizedText
import java.text.NumberFormat
import java.util.Locale

internal fun tr(language: AppLanguage, te: String, en: String): String =
    if (language == AppLanguage.TELUGU) te else en

internal fun LocalizedText.display(language: AppLanguage): String = inLanguage(language)

private val westernIndianNumberFormat = NumberFormat.getIntegerInstance(Locale("en", "IN"))

internal fun westernNumber(value: Int): String = westernIndianNumberFormat.format(value)

internal fun initials(name: String): String = name
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
    .joinToString("")
    .ifBlank { "?" }
