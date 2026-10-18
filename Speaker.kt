package com.fiza.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class Speaker(ctx: Context, private val onReady: (hindiAvailable: Boolean) -> Unit) {
    private var ready = false
    private var tts: TextToSpeech? = null
    var onDone: () -> Unit = {}

    init {
        tts = TextToSpeech(ctx.applicationContext) { status ->
            val t = tts
            if (status != TextToSpeech.SUCCESS || t == null) {
                onReady(false)
            } else {
                val hindi = Locale("hi", "IN")
                val avail = t.isLanguageAvailable(hindi)
                val ok = avail >= TextToSpeech.LANG_AVAILABLE
                if (ok) {
                    t.language = hindi
                    // Prefer an offline-capable female Hindi voice when the engine offers one.
                    t.voices
                        ?.filter { it.locale.language == "hi" }
                        ?.sortedWith(compareByDescending<android.speech.tts.Voice> { it.name.contains("female", true) }
                            .thenBy { it.isNetworkConnectionRequired })
                        ?.firstOrNull()
                        ?.let { t.voice = it }
                    t.setPitch(1.1f)
                    t.setSpeechRate(0.95f)
                }
                t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { onDone() }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { onDone() }
                })
                ready = ok
                onReady(ok)
            }
        }
    }

    fun speak(text: String) {
        if (!ready) { onDone(); return }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fiza")
    }

    fun stop() { tts?.stop() }
    fun shutdown() { tts?.shutdown(); tts = null }
}
