package com.iosexperience.feature.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosexperience.core.data.UiSettings
import com.iosexperience.core.design.components.GlassCard
import com.iosexperience.core.design.components.IOSButton
import com.iosexperience.core.design.theme.IOS

private val Suggestions = listOf("Deixe minimalista", "Tema escuro com destaque roxo", "Quero uma interface inspirada no iOS", "Ícones maiores", "Modo economia de bateria", "Grade 5 colunas")

@Composable
fun AiScreen(vm: AiViewModel, settings: UiSettings, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val msgs by vm.messages.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val canUndo by vm.canUndo.collectAsState()
    var input by remember { mutableStateOf("") }
    val list = rememberLazyListState()
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) list.animateScrollToItem(msgs.lastIndex) }
    val provider = ProviderRegistry.byId(settings.aiProvider)

    Column(modifier.fillMaxSize().imePadding().padding(horizontal = 16.dp)) {
        Text("Assistente", color = IOS.colors.label, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
        if (!settings.aiEnabled) {
            GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("O assistente está desativado.", color = IOS.colors.label)
                IOSButton("Abrir configurações de IA", onOpenSettings)
            } }
            return@Column
        }
        Text("Provedor: ${provider.displayName}. " + if (provider.id == "local") "Tudo é processado no aparelho." else "", color = IOS.colors.secondaryLabel, fontSize = 12.sp)
        LazyColumn(Modifier.weight(1f).padding(vertical = 8.dp), state = list, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (msgs.isEmpty()) item { Text("Peça mudanças na aparência em linguagem natural. Eu proponho, você confirma.", color = IOS.colors.secondaryLabel, fontSize = 14.sp) }
            items(msgs, key = { it.id }) { m ->
                Box(Modifier.fillMaxWidth(), contentAlignment = if (m.fromUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    GlassCard(Modifier.widthIn(max = 320.dp), RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(m.text, color = IOS.colors.label, fontSize = 15.sp)
                            m.actions.forEach { Text("• ${it.label}", color = IOS.colors.label.copy(alpha = 0.85f), fontSize = 13.sp) }
                            if (m.actions.isNotEmpty()) {
                                if (m.applied) Text("Aplicado ✓", color = IOS.colors.success, fontSize = 13.sp)
                                else IOSButton("Aplicar", { vm.apply(m.id) })
                            }
                        }
                    }
                }
            }
            if (loading) item { Text("Pensando…", color = IOS.colors.secondaryLabel) }
        }
        error?.let { Text(it, color = IOS.colors.destructive, fontSize = 13.sp) }
        if (canUndo) Text("Desfazer última aplicação", color = IOS.colors.accent, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 6.dp).pointerInput(Unit) { detectTapGestures { vm.undoLast() } })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            items(Suggestions) { s ->
                Text(s, color = IOS.colors.label, fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(50)).background(IOS.colors.glassTint.copy(alpha = 0.18f))
                    .pointerInput(s) { detectTapGestures { vm.send(s) } }.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(26.dp)).background(IOS.colors.glassTint.copy(alpha = 0.18f)).padding(start = 18.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
                if (input.isEmpty()) Text("Descreva a aparência desejada", color = IOS.colors.secondaryLabel, fontSize = 15.sp)
                BasicTextField(input, { input = it }, textStyle = TextStyle(color = IOS.colors.label, fontSize = 15.sp), cursorBrush = SolidColor(IOS.colors.accent), modifier = Modifier.fillMaxWidth())
            }
            Icon(Icons.AutoMirrored.Rounded.Send, "Enviar", tint = IOS.colors.accent,
                modifier = Modifier.size(48.dp).pointerInput(Unit) { detectTapGestures { vm.send(input); input = "" } }.padding(12.dp))
        }
    }
}
