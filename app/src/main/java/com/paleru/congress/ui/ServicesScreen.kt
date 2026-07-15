package com.paleru.congress.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.rounded.Agriculture
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.CivicServiceDraft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class ServiceOption(
    val id: String,
    val titleTe: String,
    val titleEn: String,
    val descriptionTe: String,
    val descriptionEn: String,
    val icon: ImageVector
) {
    fun title(language: AppLanguage) = tr(language, titleTe, titleEn)
    fun description(language: AppLanguage) = tr(language, descriptionTe, descriptionEn)
}

internal val serviceOptions = listOf(
    ServiceOption("roads", "రోడ్లు మరియు రవాణా", "Roads and transport", "గుంతలు, దెబ్బతిన్న రోడ్లు, బస్సు మరియు చేరువ సమస్యలు", "Potholes, damaged roads, bus and access issues", Icons.AutoMirrored.Rounded.AltRoute),
    ServiceOption("water", "తాగునీరు", "Drinking water", "సరఫరా, పైప్‌లైన్ లీక్, బోరు లేదా నీటి నాణ్యత", "Supply, pipeline leaks, borewell or water quality", Icons.Rounded.LocalDrink),
    ServiceOption("sanitation", "పారిశుధ్యం మరియు డ్రైనేజీ", "Sanitation and drainage", "చెత్త, డ్రైనేజీ, వీధి శుభ్రత", "Waste, drainage and street cleanliness", Icons.Rounded.WaterDrop),
    ServiceOption("electricity", "విద్యుత్ మరియు వీధి దీపాలు", "Electricity and streetlights", "వీధి దీపం, స్తంభం లేదా సరఫరా సమస్య", "Streetlight, pole or supply issue", Icons.Rounded.ElectricalServices),
    ServiceOption("land-revenue", "రెవెన్యూ మరియు భూమి", "Revenue and land", "ధరణి, సర్టిఫికెట్, రికార్డు లేదా సరిహద్దు సహాయం", "Dharani, certificates, records or boundary help", Icons.Rounded.Landscape),
    ServiceOption("housing-schemes", "గృహం మరియు సంక్షేమ పథకాలు", "Housing and welfare schemes", "అర్హత, దరఖాస్తు మరియు అధికారిక సమాచారం", "Eligibility, applications and official information", Icons.Rounded.HomeWork),
    ServiceOption("health", "ఆరోగ్యం", "Health", "ఆరోగ్య సేవ, శిబిరం లేదా అత్యవసరం కాని సహాయంపై అంతర్గత ఫాలో-అప్", "Internal follow-up for a health service, camp or non-emergency assistance", Icons.Rounded.HealthAndSafety),
    ServiceOption("education", "విద్య మరియు యువత", "Education and youth", "పాఠశాల సదుపాయాలు, స్కాలర్‌షిప్ మరియు నైపుణ్యాలు", "School facilities, scholarships and skills", Icons.Rounded.School),
    ServiceOption("farmers", "రైతు సహాయం", "Farmer support", "ఇన్‌పుట్లు, కొనుగోలు, సాగునీరు మరియు పథకాలు", "Inputs, procurement, irrigation and schemes", Icons.Rounded.Agriculture),
    ServiceOption("volunteer", "వాలంటీర్ సేవ", "Volunteer service", "సేవా శిబిరాలు, సహాయక చర్యలు మరియు సామాజిక సేవ", "Service camps, relief and community work", Icons.Rounded.VolunteerActivism),
    ServiceOption("organization", "గ్రామ / బూత్ సంస్థ", "Gram / booth organization", "కమిటీ, సభ్యుల సమన్వయం మరియు స్థానిక సంస్థ పని", "Committee, member coordination and local organization work", Icons.Rounded.Groups),
    ServiceOption("membership", "సభ్యత్వ ఫాలో-అప్", "Membership follow-up", "కొత్త సభ్యులు, ధృవీకరణ మరియు బృంద అనుసంధానం", "New members, verification and team connection", Icons.Rounded.GroupAdd),
    ServiceOption("event", "సమావేశం / శిబిరం", "Meeting / camp", "తేదీ, ప్రదేశం, బాధ్యతలు మరియు సిద్ధత", "Date, venue, ownership and preparation", Icons.Rounded.Event),
    ServiceOption("media", "మీడియా మరియు ప్రచారం", "Media and communications", "ఫోటోలు, కథనం, ఆధారాలు మరియు ప్రచురణ సమన్వయం", "Photos, story, evidence and publishing coordination", Icons.Rounded.Campaign),
    ServiceOption("other", "ఇతర ఫీల్డ్ సమస్య", "Other field issue", "వివరాలతో స్థానిక ముసాయిదా సృష్టించండి", "Create a local draft with the relevant details", Icons.Rounded.MoreHoriz)
)

@Composable
internal fun ServicesScreen(
    language: AppLanguage,
    drafts: List<CivicServiceDraft>,
    onCreateDraft: (ServiceOption) -> Unit,
    onShareDraft: (CivicServiceDraft) -> Unit,
    onOpenMap: (String) -> Unit,
    onDeleteDraft: (CivicServiceDraft) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = tr(language, "ఫీల్డ్ డెస్క్", "Field Desk"),
                subtitle = tr(
                    language,
                    "ప్రజా సమస్యలు, సంస్థ పని మరియు బృంద ఫాలో-అప్‌ను అంతర్గత నోట్‌గా నమోదు చేయండి.",
                    "Record field issues, organization work and team follow-up as private internal notes."
                )
            )
        }
        item {
            InfoBanner(
                title = tr(language, "కాంగ్రెస్ అంతర్గత పని", "Congress internal workflow"),
                body = tr(
                    language,
                    "ఈ నోట్ ఈ పరికరంలో మాత్రమే ఉంటుంది. బృందానికి అప్పగించడం, స్థితి మార్చడం మరియు ఇతర సభ్యులతో సింక్ చేయడం కోసం సురక్షిత బ్యాక్‌ఎండ్ అవసరం.",
                    "This note stays on this device. Assignment, shared status and team sync require the secure backend."
                ),
                warning = true
            )
        }
        items(serviceOptions, key = { it.id }) { option ->
            ActionTile(
                title = option.title(language),
                subtitle = option.description(language),
                icon = option.icon,
                onClick = { onCreateDraft(option) }
            )
        }
        item {
            Spacer(Modifier.height(6.dp))
            SectionHeader(
                title = tr(language, "అంతర్గత ఫీల్డ్ నోట్‌లు", "Internal field notes"),
                subtitle = tr(
                    language,
                    "ఇవి ఇంకా అప్పగించబడలేదు లేదా బృందంతో సింక్ కాలేదు.",
                    "These are not yet assigned or synced with the team."
                )
            )
        }
        if (drafts.isEmpty()) {
            item {
                EmptyState(
                    tr(language, "ఫీల్డ్ నోట్‌లు లేవు", "No field notes yet"),
                    tr(language, "పై వర్గాన్ని ఎంచుకుని మొదటి అంతర్గత నోట్ నమోదు చేయండి.", "Choose a category above and record the first internal note."),
                    Icons.Rounded.MoreHoriz
                )
            }
        } else {
            items(drafts.sortedByDescending { it.updatedAtEpochMillis }, key = { it.id }) { draft ->
                ServiceDraftCard(language, draft, onShareDraft, onOpenMap, onDeleteDraft)
            }
        }
    }
}

@Composable
private fun ServiceDraftCard(
    language: AppLanguage,
    draft: CivicServiceDraft,
    onShareDraft: (CivicServiceDraft) -> Unit,
    onOpenMap: (String) -> Unit,
    onDeleteDraft: (CivicServiceDraft) -> Unit
) {
    val category = tr(language, draft.categoryTe.ifBlank { draft.category }, draft.categoryEn.ifBlank { draft.category })
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        listOf(draft.village, draft.mandal).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(tr(language, "అంతర్గతం • సింక్ కాలేదు", "Internal • not synced"), warning = true)
            }
            Text(draft.details, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${draft.id} • ${formatTimestamp(draft.updatedAtEpochMillis)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        onOpenMap(listOf(draft.village, draft.mandal, "Khammam, Telangana, India").filter { it.isNotBlank() }.joinToString(", "))
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text(tr(language, "మ్యాప్", "Map"))
                }
                TextButton(onClick = { onShareDraft(draft) }, modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text(tr(language, "కాపీ", "Share copy"))
                }
                TextButton(onClick = { onDeleteDraft(draft) }, modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text(tr(language, "తొలగించు", "Delete"))
                }
            }
        }
    }
}

internal fun formatTimestamp(epochMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date(epochMillis))
