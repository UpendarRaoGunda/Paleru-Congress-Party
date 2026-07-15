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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Source
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.ElectionRecord
import com.paleru.congress.data.MinisterRecord
import com.paleru.congress.data.PaleruData
import com.paleru.congress.data.SourceRecord
import com.paleru.congress.ui.brand.CongressAccent
import com.paleru.congress.ui.brand.CongressBrandMark
import com.paleru.congress.ui.brand.CongressCard

@Composable
internal fun MoreScreen(
    language: AppLanguage,
    pendingProfileCorrections: Int,
    onOpenSource: (String) -> Unit,
    onClearLocalData: () -> Unit,
    onLockApp: () -> Unit,
    onResetAccess: () -> Unit,
    contentPadding: PaddingValues
) {
    var showAllElections by rememberSaveable { mutableStateOf(false) }
    var showMinisters by rememberSaveable { mutableStateOf(false) }
    var showSources by rememberSaveable { mutableStateOf(true) }
    val elections = if (showAllElections) PaleruData.elections.sortedByDescending { it.year }
    else PaleruData.elections.sortedByDescending { it.year }.take(3)

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
                title = tr(language, "సంస్థ, చరిత్ర మరియు సెట్టింగ్‌లు", "Organization, history and settings"),
                subtitle = tr(language, "పాలేరు కాంగ్రెస్ అంతర్గత సమాచారం మరియు పరికర నియంత్రణలు", "Paleru Congress intelligence and device controls")
            )
        }
        item {
            CongressCard(accent = CongressAccent.Tricolor) {
                Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    CongressBrandMark(size = 60.dp, contentDescription = tr(language, "అలంకార హస్త చిహ్నం", "Decorative hand motif"))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tr(language, "పాలేరు కాంగ్రెస్ • అంతర్గతం", "Paleru Congress • Private"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text(
                            tr(language, "పరికర PIN లాక్‌తో స్థానిక ముసాయిదాలకు ప్రాథమిక రక్షణ.", "A device PIN provides basic protection for local drafts."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onLockApp, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Rounded.Lock, contentDescription = null)
                        Spacer(Modifier.padding(3.dp))
                        Text(tr(language, "ఇప్పుడే లాక్", "Lock now"))
                    }
                    OutlinedButton(onClick = onResetAccess, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                        Spacer(Modifier.padding(3.dp))
                        Text(tr(language, "PIN రీసెట్", "Reset PIN"))
                    }
                }
            }
        }
        item {
            InfoBanner(
                title = tr(language, "స్థానిక వయస్సు సవరణలు", "Locally saved age suggestions"),
                body = tr(
                    language,
                    "ఆధారంతో సేవ్ చేసిన వయస్సు సూచనలు ఈ పరికరంలో మాత్రమే ఉన్నాయి; అవి ఇంకా సమర్పించబడలేదు లేదా ఆమోదించబడలేదు. స్థానికంగా సేవ్ చేసినవి: ${westernNumber(pendingProfileCorrections)}.",
                    "Evidence-backed age suggestions remain only on this device; they have not been submitted or approved. Saved locally: ${westernNumber(pendingProfileCorrections)}."
                )
            )
        }
        item {
            SectionHeader(
                title = tr(language, "పాలేరు ఎన్నికల చరిత్ర", "Palair election history"),
                subtitle = tr(language, "1962 నుంచి అసెంబ్లీ ఫలితాల సంక్షిప్త చరిత్ర", "A concise Assembly history from 1962"),
                actionLabel = if (showAllElections) tr(language, "తక్కువ", "Show less") else tr(language, "అన్నీ", "Show all"),
                onAction = { showAllElections = !showAllElections }
            )
        }
        items(elections, key = { "election-${it.year}" }) { election ->
            ElectionCard(language, election)
        }
        item {
            SectionHeader(
                title = tr(language, "పాలేరుకు ప్రాతినిధ్యం వహించిన మంత్రులు", "Ministers who represented Palair"),
                subtitle = tr(language, "చారిత్రక సందర్భంతో పార్టీ మరియు శాఖ", "Party and portfolio shown in historical context"),
                actionLabel = if (showMinisters) tr(language, "దాచు", "Hide") else tr(language, "చూడండి", "View"),
                onAction = { showMinisters = !showMinisters }
            )
        }
        if (showMinisters) {
            items(PaleruData.ministers, key = { it.name.en }) { minister ->
                MinisterCard(language, minister)
            }
        }
        item {
            SectionHeader(
                title = tr(language, "డేటా ఆధారాలు", "Data evidence"),
                subtitle = tr(language, "లింక్‌ను తెరిచి స్వయంగా తనిఖీ చేయండి", "Open a link and verify the record yourself"),
                actionLabel = if (showSources) tr(language, "దాచు", "Hide") else tr(language, "చూడండి", "View"),
                onAction = { showSources = !showSources }
            )
        }
        if (showSources) {
            items(PaleruData.sources, key = { it.url }) { source ->
                SourceCard(language, source, onOpenSource)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(tr(language, "అంతర్గత డేటా మరియు గోప్యత", "Internal data and privacy"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        tr(
                            language,
                            "ముసాయిదాలు ఈ ఫోన్‌లో మాత్రమే ఉంటాయి. PIN ఈ పరికరానికి ప్రాథమిక లాక్ మాత్రమే; అది కాంగ్రెస్ సభ్యత్వ ధృవీకరణ కాదు. మ్యాప్ తెరవడానికి ఈ యాప్ ప్రత్యక్ష GPS అనుమతి అడగదు.",
                            "Drafts stay on this phone. The PIN is only a basic device lock; it is not Congress membership verification. The app requests no live GPS permission to open maps."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    OutlinedButton(onClick = onClearLocalData, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(tr(language, "అన్ని స్థానిక ముసాయిదాలు తొలగించు", "Delete all local drafts"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ElectionCard(language: AppLanguage, election: ElectionRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(election.year.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(election.era.display(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusPill(
                    if (election.won) tr(language, "కాంగ్రెస్ విజయం", "Congress won") else tr(language, "కాంగ్రెస్ ఓటమి", "Congress lost"),
                    positive = election.won,
                    warning = !election.won
                )
            }
            Text(election.congressCandidate.display(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                tr(language, "కాంగ్రెస్ ఓట్లు: ${westernNumber(election.congressVotes)}", "Congress votes: ${westernNumber(election.congressVotes)}"),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                tr(
                    language,
                    "ప్రధాన ప్రత్యర్థి: ${election.opponent.display(language)} (${election.opponentParty.display(language)}) — ${westernNumber(election.opponentVotes)}",
                    "Main opponent: ${election.opponent.display(language)} (${election.opponentParty.display(language)}) — ${westernNumber(election.opponentVotes)}"
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                tr(language, "తేడా: ${westernNumber(election.margin)}", "Margin: ${westernNumber(election.margin)}"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (election.note.en.isNotBlank()) {
                Text(election.note.display(language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun MinisterCard(language: AppLanguage, minister: MinisterRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(minister.name.display(language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (minister.current) StatusPill(tr(language, "ప్రస్తుతం", "Current"), positive = true)
            }
            Text(minister.portfolio.display(language), style = MaterialTheme.typography.bodyMedium)
            Text(
                "${minister.period.display(language)} • ${minister.partyAtThatTime.display(language)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(minister.note.display(language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SourceCard(language: AppLanguage, source: SourceRecord, onOpenSource: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Source, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(source.title.display(language), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(source.detail.display(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = { onOpenSource(source.url) }, modifier = Modifier.height(48.dp)) {
                Icon(Icons.Rounded.Link, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(tr(language, "వనరు తెరవండి", "Open source"))
            }
        }
    }
}
