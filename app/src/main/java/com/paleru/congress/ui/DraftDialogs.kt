package com.paleru.congress.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.paleru.congress.data.ActivityPostDraft
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.CivicServiceDraft
import com.paleru.congress.data.ContributorRole
import com.paleru.congress.data.LocalDraftStatus
import com.paleru.congress.data.PaleruData
import com.paleru.congress.data.ProfileCorrectionDraft
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

internal data class ProfileTarget(
    val directoryId: String,
    val personName: String,
    val role: String,
    val place: String,
    val currentAge: Int? = null,
    val ageReferenceYear: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServiceDraftDialog(
    language: AppLanguage,
    initialService: ServiceOption,
    onDismiss: () -> Unit,
    onSave: (CivicServiceDraft) -> Unit
) {
    var selectedServiceId by rememberSaveable { mutableStateOf(initialService.id) }
    var mandalId by rememberSaveable { mutableStateOf("") }
    var village by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    val selectedService = serviceOptions.firstOrNull { it.id == selectedServiceId } ?: initialService
    val selectedMandal = PaleruData.mandals.firstOrNull { it.id == mandalId }
    val canSave = selectedMandal != null && village.isNotBlank() && details.length >= 10

    FullScreenForm(
        title = tr(language, "అంతర్గత ఫీల్డ్ నోట్", "Internal field note"),
        saveLabel = tr(language, "పరికరంలో నోట్ సేవ్", "Save field note"),
        closeLabel = tr(language, "మూసివేయండి", "Close"),
        canSave = canSave,
        onDismiss = onDismiss,
        onSave = {
            onSave(
                CivicServiceDraft(
                    category = selectedService.id,
                    categoryTe = selectedService.titleTe,
                    categoryEn = selectedService.titleEn,
                    mandal = selectedMandal?.nameEn.orEmpty(),
                    village = village.trim(),
                    name = name.trim(),
                    phone = phone,
                    details = details.trim(),
                    language = language,
                    status = LocalDraftStatus.LOCAL_ONLY
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                InfoBanner(
                    title = tr(language, "కాంగ్రెస్ అంతర్గత నోట్", "Congress internal note"),
                    body = tr(
                        language,
                        "ఈ నోట్ ఈ పరికరంలో మాత్రమే ఉంటుంది. బృందానికి అప్పగించడం, స్థితి ట్రాకింగ్ మరియు సింక్ కోసం సురక్షిత సంస్థ సర్వర్ అవసరం.",
                        "This note stays on this device. Team assignment, status tracking and sync require the secure organization server."
                    ),
                    warning = true
                )
            }
            item { FormLabel(tr(language, "సేవ వర్గం", "Service category")) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    serviceOptions.forEach { option ->
                        FilterChip(
                            selected = selectedServiceId == option.id,
                            onClick = { selectedServiceId = option.id },
                            label = { Text(option.title(language)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item { FormLabel(tr(language, "మండలం", "Mandal")) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaleruData.mandals.forEach { mandal ->
                        FilterChip(
                            selected = mandalId == mandal.id,
                            onClick = { mandalId = mandal.id },
                            label = { Text(if (language == AppLanguage.TELUGU) mandal.nameTe else mandal.nameEn) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "గ్రామం / వార్డు *", "Village / ward *")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it.take(1200) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "సమస్య వివరాలు *", "Issue details *")) },
                    supportingText = { Text(tr(language, "కనీసం 10 అక్షరాలు", "At least 10 characters")) },
                    minLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item { FormLabel(tr(language, "సంప్రదింపు (ఐచ్ఛికం)", "Contact (optional)")) }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "పేరు", "Name")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { candidate -> phone = candidate.filter { it in '0'..'9' }.take(15) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ఫోన్ నంబర్ (0–9 మాత్రమే)", "Phone number (0–9 only)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

private data class ActivityCategory(val id: String, val te: String, val en: String)

private val activityCategories = listOf(
    ActivityCategory("development", "అభివృద్ధి పని", "Development work"),
    ActivityCategory("public-service", "సమాజ సేవ", "Community service"),
    ActivityCategory("camp", "సేవా శిబిరం", "Service camp"),
    ActivityCategory("volunteer", "వాలంటీర్ చర్య", "Volunteer action"),
    ActivityCategory("meeting", "సమావేశం", "Meeting"),
    ActivityCategory("party-activity", "పార్టీ కార్యక్రమం", "Party activity")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivityDraftDialog(
    language: AppLanguage,
    initialRole: ContributorRole,
    initialDraft: ActivityPostDraft? = null,
    onDismiss: () -> Unit,
    onSave: (ActivityPostDraft) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("paleru_post_profile", android.content.Context.MODE_PRIVATE) }
    var authorName by rememberSaveable(initialDraft?.id) {
        mutableStateOf(initialDraft?.authorName ?: preferences.getString("author_name", "").orEmpty())
    }
    var content by rememberSaveable(initialDraft?.id) {
        mutableStateOf(initialDraft?.details.orEmpty())
    }
    var place by rememberSaveable(initialDraft?.id) {
        mutableStateOf(initialDraft?.place.orEmpty())
    }
    var mediaUri by rememberSaveable(initialDraft?.id) {
        mutableStateOf(initialDraft?.mediaUri.orEmpty())
    }
    var mediaError by rememberSaveable { mutableStateOf("") }
    val editing = initialDraft != null
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            retainPickedActivityImage(context, uri)?.let {
                mediaUri = it.toString()
                mediaError = ""
            } ?: run {
                mediaError = tr(language, "ఫోటోను జోడించలేకపోయాం. మరో ఫోటో ప్రయత్నించండి.", "The photo could not be added. Try another image.")
            }
        }
    }
    val canSave = authorName.trim().length >= 2 && content.trim().isNotEmpty()

    FullScreenForm(
        title = if (editing) tr(language, "పోస్ట్ సవరించండి", "Edit post") else tr(language, "పోస్ట్ సృష్టించండి", "Create post"),
        saveLabel = if (editing) tr(language, "మార్పులు సేవ్ చేయండి", "Save changes") else tr(language, "పోస్ట్ చేయండి", "Post"),
        closeLabel = tr(language, "మూసివేయండి", "Close"),
        canSave = canSave,
        onDismiss = {
            if (!editing && mediaUri.isNotBlank()) deleteOwnedActivityImage(context, mediaUri)
            onDismiss()
        },
        onSave = {
            val cleanContent = content.trim()
            preferences.edit().putString("author_name", authorName.trim()).apply()
            val value = initialDraft?.copy(
                title = cleanContent.lineSequence().firstOrNull().orEmpty().take(100),
                details = cleanContent,
                place = place.trim(),
                updatedAtEpochMillis = System.currentTimeMillis()
            ) ?: ActivityPostDraft(
                title = cleanContent.lineSequence().firstOrNull().orEmpty().take(100),
                details = cleanContent,
                category = "party-activity",
                categoryTe = "పార్టీ కార్యకలాపం",
                categoryEn = "Party activity",
                place = place.trim(),
                date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(java.util.Date()),
                mediaUri = mediaUri,
                mediaConsentConfirmed = mediaUri.isNotBlank(),
                authorName = authorName.trim(),
                role = initialRole,
                language = language,
                status = LocalDraftStatus.LOCAL_ONLY
            )
            onSave(value)
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InitialsAvatar(
                        name = authorName.ifBlank { "Congress" },
                        contentDescription = tr(language, "రచయిత", "Author"),
                        size = 46
                    )
                    Column {
                        Text(tr(language, "కాంగ్రెస్ నెట్‌వర్క్", "Congress Network"), fontWeight = FontWeight.Bold)
                        Text(
                            tr(language, "మీ గ్రామంలో జరిగిన పనిని సులభంగా పంచుకోండి", "Share what is happening in your village"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { if (!editing) authorName = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "మీ పేరు", "Your name")) },
                    enabled = !editing,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it.take(4000) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ఏమి జరుగుతోంది?", "What's happening?")) },
                    placeholder = { Text(tr(language, "పని, కార్యక్రమం లేదా గ్రామ వార్తను పంచుకోండి…", "Share development work, an activity, or village news…")) },
                    minLines = 6,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it.take(160) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ప్రదేశం (ఐచ్ఛికం)", "Place (optional)")) },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            if (!editing) {
                item {
                    if (mediaUri.isBlank()) {
                        OutlinedButton(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(tr(language, "ఫోటో జోడించండి (ఐచ్ఛికం)", "Add photo (optional)"))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SubcomposeAsyncImage(
                                model = Uri.parse(mediaUri),
                                contentDescription = tr(language, "ఎంచుకున్న ఫోటో", "Selected photo"),
                                modifier = Modifier.fillMaxWidth().height(260.dp),
                                contentScale = ContentScale.Crop
                            )
                            TextButton(onClick = {
                                deleteOwnedActivityImage(context, mediaUri)
                                mediaUri = ""
                            }) { Text(tr(language, "ఫోటో తొలగించండి", "Remove photo")) }
                        }
                    }
                    if (mediaError.isNotBlank()) Text(mediaError, color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Text(
                    tr(
                        language,
                        "పోస్ట్ చేయడం ద్వారా ఈ సమాచారాన్ని పంచుకునే హక్కు మీకు ఉందని నిర్ధారిస్తున్నారు. ఫోటోలోని వ్యక్తుల అనుమతి తీసుకోండి.",
                        "By posting, you confirm you may share this information. Get permission from people shown in a photo."
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegacyActivityDraftDialog(
    language: AppLanguage,
    initialRole: ContributorRole,
    onDismiss: () -> Unit,
    onSave: (ActivityPostDraft) -> Unit
) {
    var roleName by rememberSaveable { mutableStateOf(initialRole.name) }
    var categoryId by rememberSaveable { mutableStateOf(activityCategories.first().id) }
    var authorName by rememberSaveable { mutableStateOf("") }
    var directoryId by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var place by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var evidenceUrl by rememberSaveable { mutableStateOf("") }
    var mediaUri by rememberSaveable { mutableStateOf("") }
    var mediaConsentConfirmed by rememberSaveable { mutableStateOf(false) }
    var mediaError by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val retainedUri = retainPickedActivityImage(context, uri)
            if (retainedUri != null) {
                mediaUri = retainedUri.toString()
                mediaConsentConfirmed = false
                mediaError = ""
            } else {
                mediaUri = ""
                mediaConsentConfirmed = false
                mediaError = tr(
                    language,
                    "ఫోటోను సురక్షితంగా నిల్వ చేయలేకపోయాం. మరో ఫోటో ప్రయత్నించండి.",
                    "The photo could not be stored safely. Try another image."
                )
            }
        }
    }
    val role = ContributorRole.entries.firstOrNull { it.name == roleName } ?: initialRole
    val category = activityCategories.first { it.id == categoryId }
    val evidenceValid = evidenceUrl.trim().let { it.startsWith("https://") || it.startsWith("http://") }
    val hasEvidence = evidenceValid || mediaUri.isNotBlank()
    val mediaReady = mediaUri.isBlank() || mediaConsentConfirmed
    val dateValid = isValidIsoDate(date)
    val canSave = authorName.isNotBlank() && title.isNotBlank() && details.length >= 20 &&
        place.isNotBlank() && dateValid && hasEvidence && mediaReady

    FullScreenForm(
        title = tr(language, "కాంగ్రెస్ ఫీడ్ పోస్ట్", "Congress feed post"),
        saveLabel = tr(language, "అంతర్గత పోస్ట్ సేవ్", "Save internal post"),
        closeLabel = tr(language, "మూసివేయండి", "Close"),
        canSave = canSave,
        onDismiss = {
            if (mediaUri.isNotBlank()) deleteOwnedActivityImage(context, mediaUri)
            onDismiss()
        },
        onSave = {
            onSave(
                ActivityPostDraft(
                    title = title.trim(),
                    details = details.trim(),
                    category = category.id,
                    categoryTe = category.te,
                    categoryEn = category.en,
                    place = place.trim(),
                    date = date,
                    evidenceUrl = evidenceUrl.trim(),
                    mediaUri = mediaUri,
                    mediaConsentConfirmed = mediaConsentConfirmed,
                    authorName = authorName.trim(),
                    role = role,
                    authorDirectoryId = directoryId.trim(),
                    language = language,
                    status = LocalDraftStatus.PENDING_VERIFICATION
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                InfoBanner(
                    title = tr(language, "అంతర్గత ప్రచురణ నియమం", "Internal publishing rule"),
                    body = tr(
                        language,
                        "ఈ ఆఫ్‌లైన్ యాప్‌లో ఎంచుకున్న పాత్ర డ్రాఫ్ట్ అట్రిబ్యూషన్ మాత్రమే. సురక్షిత ఖాతా మరియు మోడరేటర్ ఆమోదం తర్వాతే Paleru Congress నెట్‌వర్క్‌కు ప్రచురించాలి.",
                        "The selected role is draft attribution only in this offline app. Publish to the Paleru Congress network only after secure account and moderator approval."
                    ),
                    warning = true
                )
            }
            item { FormLabel(tr(language, "సహకారి పాత్ర", "Contributor role")) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContributorRole.entries.forEach { option ->
                        FilterChip(
                            selected = role == option,
                            onClick = { roleName = option.name },
                            label = { Text(roleLabel(language, option)) }
                        )
                    }
                }
            }
            item {
                FormLabel(tr(language, "పోస్ట్ ఫోటో", "Post photo"))
            }
            item {
                if (mediaUri.isBlank()) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(tr(language, "గ్యాలరీ నుంచి ఫోటో ఎంచుకోండి", "Choose photo from gallery"))
                    }
                    if (mediaError.isNotBlank()) {
                        Text(mediaError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SubcomposeAsyncImage(
                            model = Uri.parse(mediaUri),
                            contentDescription = tr(language, "ఎంచుకున్న పోస్ట్ ఫోటో", "Selected post photo"),
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            contentScale = ContentScale.Crop
                        )
                        OutlinedButton(
                            onClick = {
                                deleteOwnedActivityImage(context, mediaUri)
                                mediaUri = ""
                                mediaConsentConfirmed = false
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(tr(language, "ఫోటో తొలగించండి", "Remove photo"))
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = mediaConsentConfirmed,
                                onCheckedChange = { mediaConsentConfirmed = it }
                            )
                            Text(
                                tr(
                                    language,
                                    "ఈ ఫోటోను కాంగ్రెస్ అంతర్గత నెట్‌వర్క్‌లో లేదా నేను ఎంచుకున్న ఆమోదిత వ్యక్తికి Android Share ద్వారా పంచుకునే అనుమతి ఉందని నిర్ధారిస్తున్నాను.",
                                    "I confirm permission to share this photo in the internal Congress network or, through Android Share, with an approved recipient I choose."
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "పూర్తి పేరు *", "Full name *")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = directoryId,
                    onValueChange = { directoryId = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "డైరెక్టరీ ఐడి (ఐచ్ఛికం)", "Directory ID (optional)")) },
                    supportingText = { Text(tr(language, "ధృవీకరణ బృందం ఇచ్చిన 0–9 / A–Z ఐడిని మాత్రమే నమోదు చేయండి", "Enter only the 0–9 / A–Z ID issued by the verification team")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item { FormLabel(tr(language, "కార్యకలాప వర్గం", "Activity category")) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    activityCategories.forEach { option ->
                        FilterChip(
                            selected = categoryId == option.id,
                            onClick = { categoryId = option.id },
                            label = { Text(tr(language, option.te, option.en)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(140) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "శీర్షిక *", "Title *")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it.take(2000) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ఏం చేశారు? ప్రజలకు లాభం ఏమిటి? *", "What was done and how did people benefit? *")) },
                    supportingText = { Text(tr(language, "కనీసం 20 అక్షరాలు; కొలవగల వివరాలు ఇవ్వండి", "At least 20 characters; include measurable details")) },
                    minLines = 6,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it.take(140) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "గ్రామం / మండలం / ప్రదేశం *", "Village / mandal / place *")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = { candidate -> date = candidate.filter { it in '0'..'9' || it == '-' }.take(10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "తేదీ YYYY-MM-DD *", "Date YYYY-MM-DD *")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text(tr(language, "ఉదాహరణ: 2026-07-15", "Example: 2026-07-15")) },
                    isError = date.isNotBlank() && !dateValid,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = evidenceUrl,
                    onValueChange = { evidenceUrl = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ఆధార URL", "Evidence URL")) },
                    supportingText = { Text(tr(language, "ఫోటో లేదా ఆధార URLలో కనీసం ఒకటి తప్పనిసరి", "At least one photo or evidence URL is required")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    isError = evidenceUrl.isNotBlank() && !evidenceValid,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

private fun isValidIsoDate(value: String): Boolean {
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(value)) return false
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }.parse(value)
    }.getOrNull() != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgeUpdateDialog(
    language: AppLanguage,
    target: ProfileTarget,
    onDismiss: () -> Unit,
    onSave: (ProfileCorrectionDraft) -> Unit
) {
    var ageText by rememberSaveable { mutableStateOf(target.currentAge?.toString().orEmpty()) }
    var yearText by rememberSaveable { mutableStateOf(target.ageReferenceYear?.toString().orEmpty()) }
    var evidenceUrl by rememberSaveable { mutableStateOf("") }
    var submitter by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val age = ageText.toIntOrNull()
    val year = yearText.toIntOrNull()
    val maxYear = Calendar.getInstance().get(Calendar.YEAR)
    val evidenceValid = evidenceUrl.trim().let { it.startsWith("https://") || it.startsWith("http://") }
    val canSave = age != null && age in 18..120 &&
        year != null && year in 1900..maxYear && evidenceValid && submitter.isNotBlank()

    FullScreenForm(
        title = tr(language, "వయస్సు నవీకరణ సూచించండి", "Suggest an age update"),
        saveLabel = tr(language, "స్థానికంగా సేవ్", "Save locally"),
        closeLabel = tr(language, "మూసివేయండి", "Close"),
        canSave = canSave,
        onDismiss = onDismiss,
        onSave = {
            onSave(
                ProfileCorrectionDraft(
                    directoryId = target.directoryId,
                    personName = target.personName,
                    role = target.role,
                    place = target.place,
                    proposedAge = age,
                    ageReferenceYear = year,
                    evidenceUrl = evidenceUrl.trim(),
                    submitterName = submitter.trim(),
                    notes = notes.trim(),
                    language = language,
                    status = LocalDraftStatus.PENDING_VERIFICATION
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 14.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(target.personName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${target.role} • ${target.place}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                InfoBanner(
                    title = tr(language, "ఆధారం తప్పనిసరి", "Evidence is required"),
                    body = tr(
                        language,
                        "పుట్టిన తేదీని ఊహించవద్దు. ఎన్నికల అఫిడవిట్, అధికారిక ప్రొఫైల్ లేదా వ్యక్తి అనుమతితో ఇచ్చిన పత్రం నుంచి వయస్సు మరియు సూచన సంవత్సరాన్ని ఇవ్వండి.",
                        "Do not infer a birth date. Provide the age and reference year from an election affidavit, official profile, or a consented document from the person."
                    ),
                    warning = true
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { value -> ageText = value.filter { it in '0'..'9' }.take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text(tr(language, "వయస్సు *", "Age *")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = ageText.isNotBlank() && (age == null || age !in 18..120),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { value -> yearText = value.filter { it in '0'..'9' }.take(4) },
                        modifier = Modifier.weight(1f),
                        label = { Text(tr(language, "సూచన సంవత్సరం *", "Reference year *")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = yearText.isNotBlank() && (year == null || year !in 1900..maxYear),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = evidenceUrl,
                    onValueChange = { evidenceUrl = it.take(500) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "ఆధార URL *", "Evidence URL *")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    isError = evidenceUrl.isNotBlank() && !evidenceValid,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = submitter,
                    onValueChange = { submitter = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "నవీకరణ ఇచ్చే వ్యక్తి పేరు *", "Name of person suggesting update *")) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(800) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr(language, "గమనికలు (ఐచ్ఛికం)", "Notes (optional)")) },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenForm(
    title: String,
    saveLabel: String,
    closeLabel: String,
    canSave: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Rounded.Close, contentDescription = closeLabel)
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(shadowElevation = 8.dp) {
                        Button(
                            onClick = onSave,
                            enabled = canSave,
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(saveLabel)
                        }
                    }
                }
            ) { innerPadding -> content(innerPadding) }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}
