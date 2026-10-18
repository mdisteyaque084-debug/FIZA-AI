package com.fiza.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.fiza.ai.ui.FizaScreen
import com.fiza.ai.ui.FizaTheme

class MainActivity : ComponentActivity() {
    private val vm: FizaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ui by vm.ui.collectAsState()
            val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) vm.onMicPressed()
            }
            FizaTheme {
                FizaScreen(
                    ui = ui,
                    onMic = {
                        val has = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        if (has) vm.onMicPressed() else askMic.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStop = vm::onStopPressed,
                    onAccessibilitySetup = { com.fiza.ai.actions.FizaAccessibilityService.openAccessibilitySettings(this) },
                    onOpenDraft = vm::openMessageDraft,
                    onDismissDraft = vm::dismissDraft,
                )
            }
        }
    }
}
