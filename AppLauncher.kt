package com.fiza.ai.actions

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

data class Outcome(val ok: Boolean, val reply: String)

/** Launches apps using official Intents. "ok" means Android accepted the launch, nothing more. */
class AppLauncher(private val ctx: Context) {

    private fun start(intent: Intent): Boolean = try {
        ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    fun open(app: KnownApp): Outcome {
        val intent: Intent? = when (app) {
            KnownApp.SETTINGS -> Intent(Settings.ACTION_SETTINGS)
            KnownApp.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            KnownApp.GALLERY -> Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY)
            KnownApp.ALARM -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            else -> app.pkg?.let { ctx.packageManager.getLaunchIntentForPackage(it) }
        }
        if (intent == null) return Outcome(false, "Maaf kijiye, ${app.label} is phone mein installed nahi mila.")
        return if (start(intent)) Outcome(true, "Ji, ${app.label} khol rahi hoon.")
        else Outcome(false, "Maaf kijiye, main ${app.label} nahi khol paayi.")
    }

    fun search(app: KnownApp, query: String): Outcome {
        val intent = when (app) {
            KnownApp.YOUTUBE -> Intent(Intent.ACTION_SEARCH)
                .setPackage("com.google.android.youtube")
                .putExtra("query", query)
            KnownApp.CHROME -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
                .setPackage("com.android.chrome")
            else -> return Outcome(false, "${app.label} mein search abhi stage 2 mein aayega.")
        }
        if (start(intent)) return Outcome(true, "Ji, ${app.label} mein \"$query\" search kar rahi hoon.")
        return webSearch(query)
    }

    fun webSearch(query: String): Outcome {
        val ok = start(Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query)) ||
            start(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))))
        return if (ok) Outcome(true, "Ji, \"$query\" search kar rahi hoon.")
        else Outcome(false, "Maaf kijiye, search nahi khul paaya.")
    }
}
