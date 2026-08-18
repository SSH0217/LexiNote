package com.example.vocanote.data

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object TranslationService {
    private val translator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
        Translation.getClient(options)
    }

    suspend fun translate(text: String): Result<String> = suspendCancellableCoroutine { cont ->
        val conditions = DownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        if (cont.isActive) cont.resume(Result.success(translated))
                    }
                    .addOnFailureListener { e ->
                        if (cont.isActive) cont.resume(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resume(Result.failure(e))
            }
    }
}
