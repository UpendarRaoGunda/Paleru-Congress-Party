package com.paleru.congress.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.paleru.congress.R
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.PaleruData
import com.paleru.congress.ui.brand.CongressAccent
import com.paleru.congress.ui.brand.CongressBrand
import com.paleru.congress.ui.brand.CongressBrandMark
import com.paleru.congress.ui.brand.CongressCard
import com.paleru.congress.ui.brand.CongressFlag

@Composable
internal fun HomeScreen(
    language: AppLanguage,
    pendingFieldDrafts: Int,
    pendingActivityDrafts: Int,
    pendingProfileUpdates: Int,
    onNewPost: () -> Unit,
    onAddFieldIssue: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenFeed: () -> Unit,
    onOpenConstituencyMap: () -> Unit,
    onOpenLeaderSource: () -> Unit,
    onUpdateLeaderAge: () -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CongressDashboardHero(language, onNewPost, onOpenFeed)
        }
        item {
            SectionHeader(
                title = tr(language, "త్వరిత చర్యలు", "Quick actions"),
                subtitle = tr(language, "పాలేరు కాంగ్రెస్ బృందం కోసం", "Built for the Paleru Congress team")
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardAction(
                        title = tr(language, "పోస్ట్ చేయండి", "Post update"),
                        subtitle = tr(language, "ఫోటోతో పని పంచుకోండి", "Share work with a photo"),
                        icon = Icons.Rounded.Campaign,
                        accent = CongressBrand.colors.saffron,
                        onClick = onNewPost,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardAction(
                        title = tr(language, "ఫీల్డ్ విషయం", "Field issue"),
                        subtitle = tr(language, "అంతర్గత ఫాలో-అప్", "Internal follow-up"),
                        icon = Icons.Rounded.AddCircle,
                        accent = CongressBrand.colors.green,
                        onClick = onAddFieldIssue,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardAction(
                        title = tr(language, "కాంగ్రెస్ నెట్‌వర్క్", "Congress network"),
                        subtitle = tr(language, "నాయకత్వం మరియు గ్రామాలు", "Leadership and villages"),
                        icon = Icons.Rounded.Groups,
                        accent = CongressBrand.colors.hand,
                        onClick = onOpenDirectory,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardAction(
                        title = tr(language, "పాలేరు మ్యాప్", "Palair map"),
                        subtitle = tr(language, "4 మండలాలు", "4 mandals"),
                        icon = Icons.Rounded.Map,
                        accent = CongressBrand.colors.saffronStrong,
                        onClick = onOpenConstituencyMap,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = tr(language, "ఈ పరికరంలో పని", "Work on this device"),
                subtitle = tr(language, "సింక్ కాని అంతర్గత ముసాయిదాలు", "Private drafts that have not synced")
            )
        }
        item {
            CongressCard(accent = CongressAccent.Tricolor) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    InternalStat(
                        value = westernNumber(pendingActivityDrafts),
                        label = tr(language, "పోస్ట్‌లు", "Posts"),
                        modifier = Modifier.weight(1f)
                    )
                    InternalStat(
                        value = westernNumber(pendingFieldDrafts),
                        label = tr(language, "ఫీల్డ్ అంశాలు", "Field items"),
                        modifier = Modifier.weight(1f)
                    )
                    InternalStat(
                        value = westernNumber(pendingProfileUpdates),
                        label = tr(language, "ప్రొఫైల్ సవరణలు", "Profile edits"),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(13.dp))
                Button(onClick = onOpenFeed, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Icon(Icons.Rounded.Campaign, contentDescription = null)
                    Spacer(Modifier.width(7.dp))
                    Text(tr(language, "కార్యకలాపాల ఫీడ్ తెరవండి", "Open activity feed"))
                }
            }
        }
        item {
            ConstituencyNetworkCard(language, onOpenDirectory)
        }
        item {
            LeadershipCard(language, onOpenLeaderSource, onUpdateLeaderAge)
        }
        item {
            CongressCard(accent = CongressAccent.Saffron) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = CongressBrand.colors.saffronStrong, modifier = Modifier.size(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tr(language, "అంతర్గత వినియోగం మాత్రమే", "Private internal use"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            tr(
                                language,
                                "ఈ యాప్ పాలేరు కాంగ్రెస్ సంస్థ, అధీకృత కార్యకర్తలు మరియు అనుమతించిన ప్రజాప్రతినిధుల సమన్వయానికి.",
                                "For Paleru Congress organization, authorized workers and approved elected representatives."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CongressDashboardHero(
    language: AppLanguage,
    onNewPost: () -> Unit,
    onOpenFeed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(RoundedCornerShape(30.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.congress_hero_ribbons),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.30f),
                        Color.Black.copy(alpha = 0.82f)
                    )
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                CongressBrandMark(size = 68.dp, contentDescription = tr(language, "అలంకార హస్త చిహ్నం", "Decorative hand motif"))
                Surface(shape = RoundedCornerShape(999.dp), color = Color.Black.copy(alpha = 0.62f)) {
                    Text(
                        tr(language, "అంతర్గతం • పాలేరు 113", "PRIVATE • PALAIR 113"),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    tr(language, "పాలేరు కాంగ్రెస్", "Paleru Congress"),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    tr(
                        language,
                        "ఒకే బృందం. నాలుగు మండలాలు. ప్రతి గ్రామంతో అనుసంధానం.",
                        "One team. Four mandals. Every village connected."
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.94f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(onClick = onNewPost, modifier = Modifier.weight(1f).height(50.dp)) {
                        Icon(Icons.Rounded.AddCircle, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(tr(language, "పోస్ట్", "Post"))
                    }
                    OutlinedButton(onClick = onOpenFeed, modifier = Modifier.weight(1f).height(50.dp)) {
                        Text(tr(language, "ఫీడ్", "View feed"), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(142.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.14f)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.padding(10.dp).size(25.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InternalStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
        Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ConstituencyNetworkCard(language: AppLanguage, onOpenDirectory: () -> Unit) {
    CongressCard(accent = CongressAccent.Green) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CongressFlag(modifier = Modifier.width(112.dp), contentDescription = tr(language, "కాంగ్రెస్ జెండా", "Congress flag"))
            Column(modifier = Modifier.weight(1f)) {
                Text(tr(language, "పాలేరు సంస్థ నెట్‌వర్క్", "Paleru organization network"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text(
                    tr(
                        language,
                        "${westernNumber(PaleruData.mandals.size)} మండలాలు • ${westernNumber(PaleruData.gramPanchayats.size)} గ్రామపంచాయతీలు",
                        "${westernNumber(PaleruData.mandals.size)} mandals • ${westernNumber(PaleruData.gramPanchayats.size)} Gram Panchayats"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = onOpenDirectory, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Icon(Icons.Rounded.Groups, contentDescription = null)
            Spacer(Modifier.width(7.dp))
            Text(tr(language, "నెట్‌వర్క్ డైరెక్టరీ", "Open network directory"))
        }
    }
}

@Composable
private fun LeadershipCard(
    language: AppLanguage,
    onOpenSource: () -> Unit,
    onUpdateAge: () -> Unit
) {
    val leader = PaleruData.currentLeader
    CongressCard(accent = CongressAccent.Blue) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SubcomposeAsyncImage(
                model = leader.photoUrl,
                contentDescription = tr(language, "పొంగులేటి శ్రీనివాస రెడ్డి అధికారిక చిత్రం", "Official portrait of Ponguleti Srinivasa Reddy"),
                modifier = Modifier.size(68.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    InitialsAvatar(leader.name.display(language), tr(language, "నాయకత్వ ప్రొఫైల్", "Leadership profile"), size = 68)
                },
                error = {
                    InitialsAvatar(leader.name.display(language), tr(language, "నాయకత్వ ప్రొఫైల్", "Leadership profile"), size = 68)
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(19.dp))
                    Text(tr(language, "నియోజకవర్గ నాయకత్వం", "Constituency leadership"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                }
                Text(leader.name.display(language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(leader.role.display(language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    tr(language, "వయస్సు ${leader.declaredAge} • ${leader.ageReferenceYear}లో ప్రకటించారు", "Age ${leader.declaredAge} • declared ${leader.ageReferenceYear}"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenSource, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(tr(language, "ప్రొఫైల్ ఆధారం", "Profile source"))
            }
            OutlinedButton(onClick = onUpdateAge, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(tr(language, "వయస్సు సవరణ", "Update age"))
            }
        }
    }
}
