package com.fiza.ai.actions

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** User-enabled screen control. It acts only on accessible UI and never bypasses lock/security screens. */
class FizaAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_SCROLLED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 120
        }
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Actions are user-command driven. */ }
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }

    fun perform(action: ScreenAction, text: String = ""): ActionResult {
        val root = rootInActiveWindow
        return when (action) {
            ScreenAction.BACK -> if (performGlobalAction(GLOBAL_ACTION_BACK)) ActionResult(true, "Pichhle page par gayi.") else ActionResult(false, "Back action nahi ho paaya.")
            ScreenAction.HOME -> if (performGlobalAction(GLOBAL_ACTION_HOME)) ActionResult(true, "Home screen khol di.") else ActionResult(false, "Home action nahi ho paaya.")
            ScreenAction.SCROLL_DOWN, ScreenAction.SCROLL_UP -> {
                if (root == null) return ActionResult(false, "Screen ke elements nahi mil rahe. App ko saamne kholkar dobara koshish karein.")
                val nodes = mutableListOf<AccessibilityNodeInfo>(); collect(root, nodes)
                val target = nodes.firstOrNull { it.isScrollable && it.isEnabled }
                val direction = if (action == ScreenAction.SCROLL_DOWN) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                val ok = target?.performAction(direction) == true
                ActionResult(ok, if (ok) "Screen scroll kar di." else "Scrollable area nahi mila; screen ko manually scroll karein.")
            }
            ScreenAction.TYPE_TEXT -> {
                if (root == null) return ActionResult(false, "Koi accessible text field nahi mila.")
                val nodes = mutableListOf<AccessibilityNodeInfo>(); collect(root, nodes)
                val target = nodes.firstOrNull { it.isFocused && it.isEditable } ?: nodes.firstOrNull { it.isEditable && it.isEnabled }
                if (target == null) ActionResult(false, "Text field nahi mila. Pehle field par tap karein.") else {
                    val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
                    val ok = target.performAction(AccessibilityNodeInfo.ACTION_FOCUS) && target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    ActionResult(ok, if (ok) "Text field mein text bhar diya; bheja nahi gaya." else "Text type nahi ho paaya.")
                }
            }
            ScreenAction.TAP_TEXT -> {
                if (root == null) return ActionResult(false, "Screen ke elements nahi mil rahe.")
                val wanted = text.trim().lowercase()
                if (wanted.isBlank()) return ActionResult(false, "Kis button ya label par tap karna hai, batayein.")
                val nodes = mutableListOf<AccessibilityNodeInfo>(); collect(root, nodes)
                val target = nodes.firstOrNull { node ->
                    val label = listOfNotNull(node.text?.toString(), node.contentDescription?.toString()).joinToString(" ").lowercase()
                    label == wanted && node.isEnabled && (node.isClickable || node.parent?.isClickable == true)
                } ?: nodes.firstOrNull { node ->
                    val label = listOfNotNull(node.text?.toString(), node.contentDescription?.toString()).joinToString(" ").lowercase()
                    label.contains(wanted) && node.isEnabled && (node.isClickable || node.parent?.isClickable == true)
                }
                val clickable = if (target?.isClickable == true) target else target?.parent
                val ok = clickable?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                ActionResult(ok, if (ok) "‘$text’ par tap kiya." else "‘$text’ naam ka clickable element nahi mila; maine koi tap nahi kiya.")
            }
            ScreenAction.CLEAR_TEXT -> {
                if (root == null) return ActionResult(false, "Koi accessible text field nahi mila.")
                val nodes = mutableListOf<AccessibilityNodeInfo>(); collect(root, nodes)
                val target = nodes.firstOrNull { it.isFocused && it.isEditable } ?: nodes.firstOrNull { it.isEditable && it.isEnabled }
                if (target == null) ActionResult(false, "Text field nahi mila.") else {
                    val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "") }
                    val ok = target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    ActionResult(ok, if (ok) "Text clear kar diya." else "Text clear nahi ho paaya.")
                }
            }
        }
    }

    private fun collect(node: AccessibilityNodeInfo, into: MutableList<AccessibilityNodeInfo>) {
        into += node
        for (i in 0 until node.childCount) node.getChild(i)?.let { collect(it, into) }
    }

    companion object {
        @Volatile private var instance: FizaAccessibilityService? = null
        fun isEnabled(): Boolean = instance != null
        fun openAccessibilitySettings(context: android.content.Context) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        fun execute(action: ScreenAction, text: String = ""): ActionResult =
            instance?.perform(action, text) ?: ActionResult(false, "Pehle Android Settings mein Fiza AI Accessibility Service enable karein.")
    }
}

enum class ScreenAction { BACK, HOME, SCROLL_DOWN, SCROLL_UP, TYPE_TEXT, CLEAR_TEXT, TAP_TEXT }
data class ActionResult(val ok: Boolean, val message: String)
