package com.fiza.ai.actions

enum class KnownApp(val label: String, val pkg: String?, val aliases: List<String>) {
    WHATSAPP("WhatsApp", "com.whatsapp", listOf("whatsapp", "whats app", "व्हाट्सएप", "व्हाट्सऐप", "वॉट्सऐप")),
    YOUTUBE("YouTube", "com.google.android.youtube", listOf("youtube", "you tube", "यूट्यूब")),
    CHROME("Chrome", "com.android.chrome", listOf("chrome", "क्रोम")),
    SETTINGS("Settings", null, listOf("settings", "setting", "सेटिंग्स", "सेटिंग")),
    CAMERA("Camera", null, listOf("camera", "कैमरा")),
    GALLERY("Gallery", null, listOf("gallery", "गैलरी")),
    ALARM("Alarm", null, listOf("alarm", "अलार्म"));
}

sealed class Command {
    object Stop : Command()
    data class Open(val app: KnownApp) : Command()
    data class OpenAndSearch(val app: KnownApp, val query: String) : Command()
    data class NeedQuery(val app: KnownApp) : Command()
    data class WebSearch(val query: String) : Command()
    /** Needs AccessibilityService (stage 2). Reported honestly, never faked. */
    data class NeedsAccessibility(val what: String) : Command()
    data class ScreenActionCommand(val action: ScreenAction, val text: String = "") : Command()
    data class DraftMessage(val recipient: String, val message: String) : Command()
    data class Unknown(val heard: String) : Command()
}

object CommandParser {
    private val WAKE = listOf("hey fiza", "hi fiza", "fiza", "फिज़ा", "फिजा", "फ़िज़ा")
    private val OPEN = setOf("kholo", "khol", "kholna", "kholiye", "open", "खोलो", "खोल", "खोलिए", "chalu", "start")
    private val STOP = setOf("ruko", "stop", "cancel", "रुको", "बंद")
    private val SEARCH = setOf("search", "सर्च", "khojo", "dhundo", "खोजो", "ढूंढो", "karo", "kro", "करो", "kijiye", "kariye")
    private val FILLER = setOf("aur", "और", "par", "पर", "pe", "google", "गूगल", "mein", "में", "ko", "se")
    private val SCROLL_DOWN = listOf("neeche scroll", "नीचे स्क्रोल", "scroll down")
    private val SCROLL_UP = listOf("upar scroll", "ऊपर स्क्रोल", "scroll up")
    private val BACK = listOf("pichhle page", "peeche jao", "back jao", "पीछे जाओ", "पिछले पेज")
    private val MESSAGE = listOf("message", "मैसेज", "msg", "चैट")
    private val TYPE = listOf("type karo", "likh do", "लिख दो", "type", "टाइप करो")

    fun parse(raw: String): Command {
        var t = raw.lowercase().replace(Regex("[,.।?!]"), " ")
        WAKE.forEach { t = t.replace(it, " ") }
        t = t.replace(Regex("\\s+"), " ").trim()
        if (t.isEmpty()) return Command.Unknown(raw)

        val words = t.split(" ")
        if (words.any { it in STOP }) return Command.Stop

        // Message content is kept as a draft; the app never sends it automatically.
        val addressedDraft = Regex("^(.+?)\\s+ko\\s+(?:message|msg|मैसेज)\\s+(?:likho|लिखो|likh do|लिख दो)\\s*:?\\s*(.+)$", RegexOption.IGNORE_CASE).find(t)
        if (addressedDraft != null) return Command.DraftMessage(addressedDraft.groupValues[1].trim(), addressedDraft.groupValues[2].trim())
        val draftMatch = Regex("(?:message|msg|मैसेज)\\s+(?:likho|लिखो|likh do|लिख दो)\\s*:?\\s*(.+)", RegexOption.IGNORE_CASE).find(t)
        if (draftMatch != null) return Command.DraftMessage("", draftMatch.groupValues[1].trim())
        if (SCROLL_DOWN.any { t.contains(it) }) return Command.ScreenActionCommand(ScreenAction.SCROLL_DOWN)
        if (SCROLL_UP.any { t.contains(it) }) return Command.ScreenActionCommand(ScreenAction.SCROLL_UP)
        if (BACK.any { t.contains(it) }) return Command.ScreenActionCommand(ScreenAction.BACK)
        if (t.contains("home screen") || t.contains("होम स्क्रीन")) return Command.ScreenActionCommand(ScreenAction.HOME)
        if (t.startsWith("type ") || t.startsWith("टाइप करो ") || t.startsWith("likh do ")) {
            val content = t.substringAfter(' ').removePrefix("karo ").trim()
            if (content.isNotBlank()) return Command.ScreenActionCommand(ScreenAction.TYPE_TEXT, content)
        }
        if (t.contains("text clear") || t.contains("text hatao") || t.contains("टेक्स्ट हटाओ"))
            return Command.ScreenActionCommand(ScreenAction.CLEAR_TEXT)
        val tapMatch = Regex("^(?:tap|टैप|click)\\s+(.+?)(?:\\s+(?:par tap karo|par click karo|dabao|दबाओ|पर टैप करो))?$", RegexOption.IGNORE_CASE).find(t)
        if (tapMatch != null && tapMatch.groupValues[1].isNotBlank())
            return Command.ScreenActionCommand(ScreenAction.TAP_TEXT, tapMatch.groupValues[1].trim())
        val tapHindi = Regex("^(.+?)\\s+(?:par tap karo|par click karo|पर टैप करो|दबाओ)$", RegexOption.IGNORE_CASE).find(t)
        if (tapHindi != null) return Command.ScreenActionCommand(ScreenAction.TAP_TEXT, tapHindi.groupValues[1].trim())
        if (MESSAGE.any { t.contains(it) } && (t.contains("rahul") || t.contains("chat kholo") || t.contains("चैट")))
            return Command.NeedsAccessibility("messaging")

        val app = KnownApp.values().firstOrNull { a -> a.aliases.any { t.contains(it) } }
        var rest = t
        app?.aliases?.forEach { rest = rest.replace(it, " ") }
        val restWords = rest.split(" ").filter { it.isNotBlank() }
        val hasSearch = restWords.any { it in setOf("search", "सर्च", "khojo", "dhundo", "खोजो", "ढूंढो") }
        val query = restWords.filter { it !in OPEN && it !in SEARCH && it !in FILLER }.joinToString(" ")

        return when {
            app != null && hasSearch && query.isNotBlank() -> Command.OpenAndSearch(app, query)
            app != null && hasSearch -> Command.NeedQuery(app)
            app != null -> Command.Open(app)
            hasSearch && query.isNotBlank() -> Command.WebSearch(query)
            else -> Command.Unknown(raw)
        }
    }
}
