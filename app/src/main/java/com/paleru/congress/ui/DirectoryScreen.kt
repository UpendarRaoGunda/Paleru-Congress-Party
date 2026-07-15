package com.paleru.congress.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.GramPanchayatRecord
import com.paleru.congress.data.MandalRecord
import com.paleru.congress.data.PaleruData
import com.paleru.congress.ui.brand.CongressAccent
import com.paleru.congress.ui.brand.CongressBrand
import com.paleru.congress.ui.brand.CongressBrandMark
import com.paleru.congress.ui.brand.CongressCard
import com.paleru.congress.ui.brand.CongressFlag

private enum class NetworkMode { CONGRESS, SARPANCH_REFERENCE }

@Composable
internal fun DirectoryScreen(
    language: AppLanguage,
    onOpenMap: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onUpdateAge: (ProfileTarget) -> Unit,
    onContributeCorrection: (String, String) -> Unit,
    contentPadding: PaddingValues
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedMandalId by rememberSaveable { mutableStateOf<String?>(null) }
    var modeName by rememberSaveable { mutableStateOf(NetworkMode.CONGRESS.name) }
    val mode = NetworkMode.entries.firstOrNull { it.name == modeName } ?: NetworkMode.CONGRESS
    val selectedMandals = if (selectedMandalId == null) PaleruData.mandals
    else PaleruData.mandals.filter { it.id == selectedMandalId }
    val filteredEntries = selectedMandals.flatMap { mandal ->
        mandal.gramPanchayats.filter { gp ->
            query.isBlank() || listOf(
                gp.nameTe,
                gp.nameEn,
                gp.sarpanchOfficialName,
                mandal.nameTe,
                mandal.nameEn
            ).any { it.contains(query.trim(), ignoreCase = true) }
        }.map { mandal to it }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { NetworkHero(language) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == NetworkMode.CONGRESS,
                    onClick = { modeName = NetworkMode.CONGRESS.name },
                    label = { Text(tr(language, "కాంగ్రెస్ నాయకత్వం", "Congress leadership")) },
                    leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) }
                )
                FilterChip(
                    selected = mode == NetworkMode.SARPANCH_REFERENCE,
                    onClick = { modeName = NetworkMode.SARPANCH_REFERENCE.name },
                    label = { Text(tr(language, "సర్పంచ్ రిఫరెన్స్", "Sarpanch reference")) },
                    leadingIcon = { Icon(Icons.Rounded.Groups, contentDescription = null) }
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        if (mode == NetworkMode.CONGRESS) tr(language, "మండలం లేదా నాయకుని పేరు", "Search mandal or leader")
                        else tr(language, "గ్రామం లేదా సర్పంచ్ పేరు", "Search village or Sarpanch")
                    )
                },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMandalId == null,
                    onClick = { selectedMandalId = null },
                    label = { Text(tr(language, "అన్ని మండలాలు", "All mandals")) }
                )
                PaleruData.mandals.forEach { mandal ->
                    FilterChip(
                        selected = selectedMandalId == mandal.id,
                        onClick = { selectedMandalId = mandal.id },
                        label = { Text(if (language == AppLanguage.TELUGU) mandal.nameTe else mandal.nameEn) }
                    )
                }
            }
        }

        if (mode == NetworkMode.CONGRESS) {
            item {
                SectionHeader(
                    title = tr(language, "మండల కాంగ్రెస్ బృందం", "Mandal Congress team"),
                    subtitle = tr(language, "పాత్ర, ప్రదేశం, మ్యాప్ మరియు ప్రొఫైల్ నవీకరణ", "Role, jurisdiction, map and profile updates")
                )
            }
            val leaders = selectedMandals.filter { mandal ->
                query.isBlank() || listOf(
                    mandal.nameTe,
                    mandal.nameEn,
                    mandal.congressPresidentTe,
                    mandal.congressPresidentEn
                ).any { it.contains(query.trim(), ignoreCase = true) }
            }
            if (leaders.isEmpty()) {
                item {
                    EmptyState(
                        tr(language, "నాయకత్వ ఫలితం లేదు", "No leadership match"),
                        tr(language, "వేరే పేరు లేదా మండలంతో వెతకండి.", "Try another name or mandal."),
                        Icons.Rounded.PersonSearch
                    )
                }
            } else {
                items(leaders, key = { "president-${it.id}" }) { mandal ->
                    MandalPresidentNetworkCard(language, mandal, onOpenMap, onOpenSource, onUpdateAge, onContributeCorrection)
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        tr(
                            language,
                            "రిఫరెన్స్ మాత్రమే: గ్రామపంచాయతీ ఎన్నికలు పార్టీలకు అతీతం. ధృవీకరించిన అనుమతి లేకుండా సర్పంచ్‌కు కాంగ్రెస్ పాత్ర ఇవ్వబడదు.",
                            "Reference only: Gram Panchayat elections are non-party. A Sarpanch receives no Congress role without separate verified authorization."
                        ),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            item {
                SectionHeader(
                    title = tr(language, "గ్రామపంచాయతీ రిఫరెన్స్", "Gram Panchayat reference"),
                    subtitle = tr(
                        language,
                        "2025 TGSEC • ${westernNumber(filteredEntries.size)} ఫలితాలు",
                        "2025 TGSEC • ${westernNumber(filteredEntries.size)} matches"
                    ),
                    actionLabel = tr(language, "వనరు", "Source"),
                    onAction = { onOpenSource(PaleruData.sarpanchResultsUrl) }
                )
            }
            if (filteredEntries.isEmpty()) {
                item {
                    EmptyState(
                        tr(language, "ఫలితాలు లేవు", "No matches"),
                        tr(language, "వేరే గ్రామం, మండలం లేదా పేరుతో వెతకండి.", "Try another village, mandal or person name."),
                        Icons.Rounded.PersonSearch
                    )
                }
            } else {
                items(filteredEntries, key = { (mandal, gp) -> "${mandal.id}-${gp.nameEn}" }) { (mandal, gp) ->
                    SarpanchNetworkRow(language, mandal, gp, onOpenMap, onUpdateAge, onContributeCorrection)
                }
            }
        }
    }
}

@Composable
private fun NetworkHero(language: AppLanguage) {
    CongressCard(accent = CongressAccent.Tricolor) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CongressBrandMark(size = 66.dp, contentDescription = tr(language, "అలంకార హస్త చిహ్నం", "Decorative hand motif"))
            Column(modifier = Modifier.weight(1f)) {
                Text(tr(language, "పాలేరు కాంగ్రెస్ నెట్‌వర్క్", "Paleru Congress Network"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    tr(
                        language,
                        "4 మండలాలు • 134 గ్రామపంచాయతీలు • ఒకే సంస్థ డైరెక్టరీ",
                        "4 mandals • 134 Gram Panchayats • one organization directory"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MandalPresidentNetworkCard(
    language: AppLanguage,
    mandal: MandalRecord,
    onOpenMap: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onUpdateAge: (ProfileTarget) -> Unit,
    onContributeCorrection: (String, String) -> Unit
) {
    val name = if (language == AppLanguage.TELUGU) mandal.congressPresidentTe else mandal.congressPresidentEn
    val needsConfirmation = mandal.id == "thirumalayapalem"
    CongressCard(accent = if (needsConfirmation) CongressAccent.Saffron else CongressAccent.Green) {
        Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
            NetworkAvatar(
                name = name,
                photoUrl = mandal.presidentPhotoUrl,
                contentDescription = tr(language, "$name చిత్రం", "Portrait of $name"),
                size = 58
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    tr(language, "${mandal.nameTe} మండల కాంగ్రెస్ అధ్యక్షుడు", "${mandal.nameEn} Mandal Congress president"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                StatusPill(
                    if (needsConfirmation) tr(language, "పాత్ర నిర్ధారణ పెండింగ్", "Role confirmation pending")
                    else tr(language, "2026 ప్రజా నివేదిక", "Reported in 2026"),
                    warning = true
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(ageText(language, mandal.presidentDeclaredAge, mandal.presidentAgeReferenceYear), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = { onOpenMap(mandal.mapQuery) }, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text(tr(language, "మ్యాప్", "Map"))
            }
            OutlinedButton(
                onClick = {
                    onUpdateAge(
                        ProfileTarget(
                            directoryId = "mandal-president-${mandal.id}",
                            personName = name,
                            role = tr(language, "మండల కాంగ్రెస్ అధ్యక్షుడు", "Mandal Congress president"),
                            place = mandal.nameEn,
                            currentAge = mandal.presidentDeclaredAge,
                            ageReferenceYear = mandal.presidentAgeReferenceYear
                        )
                    )
                },
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text(tr(language, "వయస్సు", "Age")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { onOpenSource(mandal.presidentSourceUrl) }, modifier = Modifier.height(48.dp)) {
                Text(tr(language, "పాత్ర ఆధారం", "Role evidence"))
            }
            TextButton(onClick = { onContributeCorrection(name, mandal.nameEn) }, modifier = Modifier.height(48.dp)) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(tr(language, "సవరణ అభ్యర్థన పంచుకోండి", "Share correction request"))
            }
        }
    }
}

@Composable
private fun SarpanchNetworkRow(
    language: AppLanguage,
    mandal: MandalRecord,
    gp: GramPanchayatRecord,
    onOpenMap: (String) -> Unit,
    onUpdateAge: (ProfileTarget) -> Unit,
    onContributeCorrection: (String, String) -> Unit
) {
    val village = if (language == AppLanguage.TELUGU) gp.nameTe else gp.nameEn
    val mapQuery = "${gp.nameEn}, ${mandal.nameEn} Mandal, Khammam, Telangana, India"
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                NetworkAvatar(
                    name = gp.sarpanchOfficialName,
                    photoUrl = gp.photoUrl,
                    contentDescription = tr(language, "సర్పంచ్ చిత్రం", "Sarpanch portrait"),
                    size = 50
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(gp.sarpanchOfficialName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        tr(language, "$village • సర్పంచ్", "$village • Sarpanch"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(ageText(language, gp.declaredAge, gp.ageReferenceYear), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onOpenMap(mapQuery) }) {
                    Icon(Icons.Rounded.Map, contentDescription = tr(language, "మ్యాప్", "Map"), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        onUpdateAge(
                            ProfileTarget(
                                directoryId = "sarpanch-${mandal.id}-${gp.nameEn}",
                                personName = gp.sarpanchOfficialName,
                                role = tr(language, "సర్పంచ్", "Sarpanch"),
                                place = "${gp.nameEn}, ${mandal.nameEn}",
                                currentAge = gp.declaredAge,
                                ageReferenceYear = gp.ageReferenceYear
                            )
                        )
                    },
                    modifier = Modifier.height(44.dp)
                ) { Text(tr(language, "వయస్సు నవీకరణ", "Update age")) }
                TextButton(
                    onClick = { onContributeCorrection(gp.sarpanchOfficialName, "${gp.nameEn}, ${mandal.nameEn}") },
                    modifier = Modifier.height(44.dp)
                ) { Text(tr(language, "సవరణ అభ్యర్థన", "Correction request")) }
            }
        }
    }
}

@Composable
private fun NetworkAvatar(
    name: String,
    photoUrl: String?,
    contentDescription: String,
    size: Int
) {
    if (photoUrl.isNullOrBlank()) {
        InitialsAvatar(name, contentDescription, size = size)
    } else {
        SubcomposeAsyncImage(
            model = photoUrl,
            contentDescription = contentDescription,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = { InitialsAvatar(name, contentDescription, size = size) },
            error = { InitialsAvatar(name, contentDescription, size = size) }
        )
    }
}

private fun ageText(language: AppLanguage, age: Int?, referenceYear: Int?): String =
    if (age != null && referenceYear != null) {
        tr(language, "వయస్సు $age • ${referenceYear}లో ప్రకటించారు", "Age $age • declared $referenceYear")
    } else {
        tr(language, "వయస్సు నవీకరించలేదు", "Age not updated")
    }
