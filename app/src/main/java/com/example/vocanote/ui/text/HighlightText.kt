package com.example.vocanote.ui.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun highlightWordsInSentence(sentence: String, words: List<String>, color: Color): AnnotatedString =
    buildAnnotatedString {
        append(sentence)
        words.forEach { word ->
            if (word.isBlank()) return@forEach
            Regex(Regex.escape(word), RegexOption.IGNORE_CASE).findAll(sentence).forEach { match ->
                addStyle(
                    SpanStyle(color = color, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }
