package com.paleru.congress.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paleru.congress.R
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.DevicePinAccess
import com.paleru.congress.data.DevicePinCopy
import com.paleru.congress.data.DevicePinStage
import com.paleru.congress.ui.brand.CongressBrandMark
import com.paleru.congress.ui.brand.CongressTricolorRail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PrivateAccessScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    access: DevicePinAccess,
    onUnlocked: () -> Unit,
    onLocalDataReset: () -> Unit
) {
    var state by remember { mutableStateOf(access.uiState()) }
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (working) return
        scope.launch {
            working = true
            val result = withContext(Dispatchers.Default) {
                if (state.stage == DevicePinStage.CREATE_PIN) access.createPin(pin, confirmation)
                else access.unlock(pin)
            }
            working = false
            state = result.nextState
            if (result.succeeded) {
                error = ""
                pin = ""
                confirmation = ""
                onUnlocked()
            } else {
                error = result.message.inLanguage(language)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Image(
            painter = painterResource(R.drawable.congress_hero_ribbons),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = if (MaterialTheme.colorScheme.background.luminance() < 0.3f) 0.18f else 0.34f
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                LanguageToggle(language, onLanguageChange)
            }
            Spacer(Modifier.height(22.dp))
            CongressBrandMark(size = 92.dp, contentDescription = tr(language, "అలంకార హస్త చిహ్నం", "Decorative hand motif"))
            Spacer(Modifier.height(16.dp))
            Text(
                tr(language, "పాలేరు కాంగ్రెస్", "Paleru Congress"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                tr(language, "అంతర్గత సమన్వయ యాప్", "Private Coordination App"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(22.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CongressTricolorRail(modifier = Modifier.fillMaxWidth(), height = 6.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(
                                if (state.stage == DevicePinStage.CREATE_PIN) Icons.Rounded.Shield else Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp).size(26.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(state.title.inLanguage(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(state.instruction.inLanguage(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (state.stage != DevicePinStage.RECOVERY_REQUIRED) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { value ->
                                pin = value.filter { it in '0'..'9' }.take(6)
                                error = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr(language, "4–6 అంకెల PIN", "4–6 digit PIN")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = if (state.stage == DevicePinStage.CREATE_PIN) ImeAction.Next else ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = error.isNotBlank(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        if (state.stage == DevicePinStage.CREATE_PIN) {
                            OutlinedTextField(
                                value = confirmation,
                                onValueChange = { value ->
                                    confirmation = value.filter { it in '0'..'9' }.take(6)
                                    error = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(tr(language, "PIN మళ్లీ నమోదు", "Confirm PIN")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { submit() }),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                isError = error.isNotBlank(),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    if (error.isNotBlank()) {
                        Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    }

                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(
                            state.identityNotice.inLanguage(language),
                            modifier = Modifier.padding(13.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    if (state.stage == DevicePinStage.RECOVERY_REQUIRED) {
                        Button(onClick = { showResetConfirmation = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(tr(language, "స్థానిక యాక్సెస్ రీసెట్", "Reset local access"))
                        }
                    } else {
                        Button(
                            onClick = { submit() },
                            enabled = !working && pin.length in 4..6 && (state.stage != DevicePinStage.CREATE_PIN || confirmation.length in 4..6),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            if (working) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(if (state.stage == DevicePinStage.CREATE_PIN) Icons.Rounded.Shield else Icons.Rounded.Lock, contentDescription = null)
                            }
                            Spacer(Modifier.padding(4.dp))
                            Text(if (state.stage == DevicePinStage.CREATE_PIN) tr(language, "PIN సృష్టించి కొనసాగండి", "Create PIN and continue") else tr(language, "అన్‌లాక్", "Unlock"))
                        }
                        if (state.canResetWithLocalDataWipe) {
                            TextButton(onClick = { showResetConfirmation = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                Text(tr(language, "PIN మర్చిపోయారా? స్థానిక డేటా రీసెట్", "Forgot PIN? Reset local data"))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                tr(language, "పాలేరు 113 • కాంగ్రెస్ అంతర్గత వినియోగం", "Palair 113 • Congress internal use"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text(tr(language, "PIN మరియు స్థానిక డేటా రీసెట్ చేయాలా?", "Reset PIN and local data?")) },
            text = { Text(DevicePinCopy.resetWarning.inLanguage(language)) },
            confirmButton = {
                TextButton(onClick = {
                    if (!working) {
                        scope.launch {
                            working = true
                            val result = withContext(Dispatchers.Default) { access.resetPinAndWipeAllLocalData() }
                            working = false
                            showResetConfirmation = false
                            state = result.nextState
                            error = if (result.succeeded) "" else result.message.inLanguage(language)
                            pin = ""
                            confirmation = ""
                            if (result.succeeded) onLocalDataReset()
                        }
                    }
                }) { Text(tr(language, "అన్నీ తొలగించి రీసెట్", "Delete all and reset"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text(tr(language, "రద్దు", "Cancel")) }
            }
        )
    }
}
