package com.fiza.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fiza.ai.actions.AppLauncher
import com.fiza.ai.actions.Command
import com.fiza.ai.actions.CommandParser
import com.fiza.ai.actions.FizaAccessibilityService
import com.fiza.ai.actions.ScreenAction
import android.content.Intent
import android.net.Uri
import com.fiza.ai.voice.Speaker
import com.fiza.ai.voice.VoiceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class FizaStatus { IDLE, LISTENING, THINKING, SPEAKING }

data class FizaUi(
    val status: FizaStatus = FizaStatus.IDLE,
    val transcript: String = "",
    val reply: String = "Namaste! Mic dabaiye aur boliye.",
    val action: String = "",
    val history: List<String> = emptyList(),
    val hindiVoiceMissing: Boolean = false,
    val draftRecipient: String? = null,
    val draftMessage: String? = null,
)

class FizaViewModel(app: Application) : AndroidViewModel(app) {
    private val _ui = MutableStateFlow(FizaUi())
    val ui: StateFlow<FizaUi> = _ui.asStateFlow()

    private val launcher = AppLauncher(app)

    private val speaker = Speaker(app) { hindiOk ->
        _ui.update { it.copy(hindiVoiceMissing = !hindiOk) }
    }.also { s ->
        s.onDone = { _ui.update { it.copy(status = FizaStatus.IDLE) } }
    }

    private val voice = VoiceController(
        ctx = app,
        onPartial = { t -> _ui.update { it.copy(transcript = t) } },
        onFinal = { t -> handle(t) },
        onError = { msg -> say(msg) },
    )

    fun onMicPressed() {
        speaker.stop()
        _ui.update { it.copy(status = FizaStatus.LISTENING, transcript = "", action = "") }
        voice.start()
    }

    fun onStopPressed() {
        voice.stop()
        speaker.stop()
        _ui.update { it.copy(status = FizaStatus.IDLE, action = "Roka gaya") }
    }

    private fun handle(heard: String) {
        _ui.update { it.copy(status = FizaStatus.THINKING, transcript = heard) }
        val cmd = CommandParser.parse(heard)
        val (action, reply) = when (cmd) {
            is Command.Stop -> "Ruk gayi" to "Theek hai, ruk gayi."
            is Command.Open -> "${cmd.app.label} kholna" to launcher.open(cmd.app).reply
            is Command.OpenAndSearch -> "${cmd.app.label} search: ${cmd.query}" to launcher.search(cmd.app, cmd.query).reply
            is Command.NeedQuery -> "Search" to "Kya search karun? Phir se boliye."
            is Command.WebSearch -> "Web search: ${cmd.query}" to launcher.webSearch(cmd.query).reply
            is Command.NeedsAccessibility -> "Screen control" to
                "Is command ke liye supported screen control chahiye. Accessibility setup button se Fiza AI service enable karein, aur WhatsApp mein contact khud verify karein."
            is Command.ScreenActionCommand -> {
                val result = FizaAccessibilityService.execute(cmd.action, cmd.text)
                "Screen action: ${cmd.action.name}" to result.message
            }
            is Command.DraftMessage -> {
                _ui.update { it.copy(draftRecipient = cmd.recipient.ifBlank { null }, draftMessage = cmd.message) }
                "Message draft review" to "Message draft taiyar hai. Main ise khud se send nahi karungi. Neeche review karke WhatsApp mein kholen."
            }
            is Command.Unknown -> "Samajh nahi aaya" to "Maaf kijiye, main samajh nahi paayi. Dobara boliye."
        }
        _ui.update { it.copy(action = action, history = (listOf(heard) + it.history).take(10)) }
        say(reply)
    }

    fun dismissDraft() { _ui.update { it.copy(draftRecipient = null, draftMessage = null) } }

    fun openMessageDraft() {
        val message = _ui.value.draftMessage ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(message))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            getApplication<Application>().startActivity(intent)
            say("WhatsApp ya browser mein draft kholne ki request ki hai. Sahi contact khud select karke message check karein; send aapko manually karna hoga.")
        } catch (_: Exception) {
            say("Draft open nahi ho paaya. WhatsApp installed hai ya nahi check karein.")
        }
        dismissDraft()
    }

    private fun say(text: String) {
        _ui.update { it.copy(status = FizaStatus.SPEAKING, reply = text) }
        speaker.speak(text)
    }

    override fun onCleared() {
        voice.stop()
        speaker.shutdown()
    }
}
