package com.fiza.ai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiza.ai.FizaStatus
import com.fiza.ai.FizaUi

private val Ink = Color(0xFF100D1D)
private val Panel = Color(0xFF1D1830)
private val Violet = Color(0xFF9B7BFF)
private val Pink = Color(0xFFFF83C6)

@Composable
fun FizaTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) darkColorScheme(primary=Violet, secondary=Pink, background=Ink, surface=Panel)
    else darkColorScheme(primary=Violet, secondary=Pink, background=Ink, surface=Panel)
    MaterialTheme(colorScheme=scheme, content=content)
}

private fun statusText(s: FizaStatus) = when(s) {
    FizaStatus.IDLE -> "Aapki madad ke liye taiyaar"
    FizaStatus.LISTENING -> "Sunn rahi hoon…"
    FizaStatus.THINKING -> "Soch rahi hoon…"
    FizaStatus.SPEAKING -> "Baat kar rahi hoon…"
}

@Composable
fun FizaScreen(
    ui: FizaUi,
    onMic: () -> Unit,
    onStop: () -> Unit,
    onAccessibilitySetup: () -> Unit,
    onOpenDraft: () -> Unit,
    onDismissDraft: () -> Unit,
) {
    Surface(color=Ink, modifier=Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF21163E), Ink, Color(0xFF171126))))) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal=20.dp, vertical=14.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically, modifier=Modifier.fillMaxWidth()) {
                    Box(Modifier.size(46.dp).background(Brush.linearGradient(listOf(Violet, Pink)), RoundedCornerShape(16.dp)), contentAlignment=Alignment.Center) {
                        Text("F", color=Color.White, fontSize=27.sp, fontWeight=FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Fiza AI", fontSize=23.sp, fontWeight=FontWeight.Bold, color=Color.White)
                        Text("Aapki apni Hindi assistant", fontSize=12.sp, color=Color(0xFFB8ADC9))
                    }
                    Box(Modifier.border(1.dp, Color(0xFF51426F), CircleShape).padding(horizontal=10.dp, vertical=6.dp)) {
                        Text("● ONLINE", color=Color(0xFF8EE6BD), fontSize=10.sp, fontWeight=FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF342456), Color(0xFF211A36))), RoundedCornerShape(28.dp)).border(1.dp, Color(0xFF4B3A70), RoundedCornerShape(28.dp)).padding(vertical=22.dp, horizontal=16.dp), contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        PremiumOrb(ui.status)
                        Spacer(Modifier.height(12.dp))
                        Text(statusText(ui.status), color=Color.White, fontSize=17.sp, fontWeight=FontWeight.SemiBold)
                        Text("Boliye, main sun rahi hoon", color=Color(0xFFBFB3D7), fontSize=12.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("BAAT-CHEET", color=Color(0xFFB7A5DE), fontSize=11.sp, fontWeight=FontWeight.Bold, letterSpacing=2.sp)
                Spacer(Modifier.height(8.dp))
                Column(Modifier.weight(1f).fillMaxWidth().background(Color(0x991A1528), RoundedCornerShape(22.dp)).padding(14.dp)) {
                    if (ui.transcript.isNotBlank()) {
                        Text("AAPNE KAHA", color=Color(0xFFB7A5DE), fontSize=10.sp, fontWeight=FontWeight.Bold)
                        Text(ui.transcript, color=Color.White, fontSize=15.sp, modifier=Modifier.fillMaxWidth().padding(top=5.dp, bottom=12.dp))
                        Divider(color=Color(0xFF3A304D))
                    }
                    Text("FIZA KA JAWAB", color=Color(0xFFB7A5DE), fontSize=10.sp, fontWeight=FontWeight.Bold, modifier=Modifier.padding(top=8.dp))
                    Text(ui.reply, color=Color(0xFFF4F0FC), fontSize=15.sp, lineHeight=22.sp, modifier=Modifier.fillMaxWidth().padding(top=6.dp))
                    if (ui.action.isNotBlank()) Text("✦  ${ui.action}", color=Pink, fontSize=12.sp, modifier=Modifier.padding(top=10.dp))
                    if (ui.history.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp)); Divider(color=Color(0xFF3A304D)); Spacer(Modifier.height(8.dp))
                        Text("RECENT COMMANDS", color=Color(0xFFB7A5DE), fontSize=10.sp, fontWeight=FontWeight.Bold)
                        ui.history.take(3).forEach { Text("•  $it", color=Color(0xFFD2C9E4), fontSize=12.sp, modifier=Modifier.padding(top=5.dp)) }
                    }
                    if (ui.hindiVoiceMissing) Text("Hindi voice ke liye Android Settings → Text-to-speech mein Hindi voice install karein.", color=Pink, fontSize=12.sp, modifier=Modifier.padding(top=8.dp))
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick=onStop, modifier=Modifier.height(54.dp), shape=RoundedCornerShape(18.dp), colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) { Text("■  Rokein") }
                    Button(onClick=onMic, modifier=Modifier.weight(1f).height(58.dp), shape=RoundedCornerShape(20.dp), colors=ButtonDefaults.buttonColors(containerColor=Violet, contentColor=Color.White)) {
                        Text("🎙  Fiza se baat karein", fontSize=15.sp, fontWeight=FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick=onAccessibilitySetup, modifier=Modifier.fillMaxWidth()) { Text("⚙  Phone control permission setup", color=Color(0xFFC8B8F1), fontSize=12.sp) }
                Text("Screen control optional hai. Sirf bharosemand commands ke liye enable karein.", color=Color(0xFF8F849F), fontSize=10.sp, textAlign=TextAlign.Center, modifier=Modifier.fillMaxWidth())
            }
        }
    }
    if (ui.draftMessage != null) {
        AlertDialog(onDismissRequest=onDismissDraft, title={Text("Message bhejne se pehle review")},
            text={ Column { Text("Recipient: ${ui.draftRecipient ?: "WhatsApp mein khud select karein"}"); Spacer(Modifier.height(8.dp)); Text(ui.draftMessage); Spacer(Modifier.height(8.dp)); Text("Fiza message automatically send nahi karegi. Contact verify karein aur WhatsApp mein manually Send dabayein.", fontSize=12.sp) } },
            confirmButton={ Button(onClick=onOpenDraft) { Text("WhatsApp mein draft kholen") } },
            dismissButton={ TextButton(onClick=onDismissDraft) { Text("Cancel") } })
    }
}

@Composable
private fun PremiumOrb(status: FizaStatus) {
    val transition = rememberInfiniteTransition(label="fiza-orb")
    val pulse by transition.animateFloat(initialValue=0.94f, targetValue=if(status==FizaStatus.IDLE) 1.04f else 1.16f,
        animationSpec=infiniteRepeatable(animation=tween(850), repeatMode=RepeatMode.Reverse), label="pulse")
    Box(Modifier.size(126.dp).scale(pulse).background(Brush.radialGradient(listOf(Color(0x66FF83C6), Color(0x229B7BFF), Color.Transparent)), CircleShape), contentAlignment=Alignment.Center) {
        Box(Modifier.size(92.dp).background(Brush.linearGradient(listOf(Pink, Violet, Color(0xFF6045D6))), CircleShape).border(2.dp, Color(0x88FFFFFF), CircleShape), contentAlignment=Alignment.Center) {
            Text(if(status==FizaStatus.LISTENING) "♫" else "F", color=Color.White, fontSize=42.sp, fontWeight=FontWeight.Bold)
        }
    }
}
