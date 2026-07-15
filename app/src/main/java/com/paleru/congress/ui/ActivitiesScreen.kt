package com.paleru.congress.ui

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.paleru.congress.R
import com.paleru.congress.data.ActivityPostDraft
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.ContributorRole

private enum class FeedFilter { ALL, DEVELOPMENT, PARTY, SERVICE, CAMPS, SAVED }

@Composable
internal fun ActivitiesScreen(
    language: AppLanguage,
    drafts: List<ActivityPostDraft>,
    likedPostIds: Set<String>,
    bookmarkedPostIds: Set<String>,
    feedOnline: Boolean,
    isRefreshing: Boolean,
    onCreateDraft: (ContributorRole) -> Unit,
    onShareDraft: (ActivityPostDraft) -> Unit,
    onEditDraft: (ActivityPostDraft) -> Unit,
    onDeleteDraft: (ActivityPostDraft) -> Unit,
    onOpenComments: (ActivityPostDraft) -> Unit,
    onRefresh: () -> Unit,
    onOpenEvidence: (String) -> Unit,
    onOpenMap: (String) -> Unit,
    onReaction: (ActivityPostDraft, String) -> Unit,
    onToggleBookmark: (ActivityPostDraft) -> Unit,
    contentPadding: PaddingValues
) {
    var filterName by rememberSaveable { mutableStateOf(FeedFilter.ALL.name) }
    val filter = FeedFilter.entries.firstOrNull { it.name == filterName } ?: FeedFilter.ALL
    val filteredDrafts = drafts.filter { draft ->
        when (filter) {
            FeedFilter.ALL -> true
            FeedFilter.DEVELOPMENT -> draft.category == "development"
            FeedFilter.PARTY -> draft.category in setOf("party-activity", "meeting")
            FeedFilter.SERVICE -> draft.category in setOf("public-service", "volunteer")
            FeedFilter.CAMPS -> draft.category == "camp"
            FeedFilter.SAVED -> draft.id in bookmarkedPostIds
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FeedHeader(
                language = language,
                filter = filter,
                onFilterChange = { filterName = it.name },
                onCreateDraft = { onCreateDraft(ContributorRole.PARTY_MEMBER) }
            )
        }
        item {
            FeedComposer(language = language, onCreateDraft = { onCreateDraft(ContributorRole.PARTY_MEMBER) })
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedFilter.entries.forEach { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filterName = option.name },
                        label = { Text(filterLabel(language, option)) }
                    )
                }
            }
        }
        item {
            InternalFeedNotice(language, feedOnline, isRefreshing, onRefresh)
        }
        if (filter == FeedFilter.ALL) {
            item { CongressNetworkWelcomePost(language) }
            item { PostingStandardPost(language, onCreateDraft) }
        }
        if (filteredDrafts.isEmpty() && filter != FeedFilter.ALL) {
            item {
                EmptyFeedCard(
                    language = language,
                    savedFilter = filter == FeedFilter.SAVED,
                    onCreateDraft = { onCreateDraft(ContributorRole.PARTY_MEMBER) }
                )
            }
        } else if (filteredDrafts.isNotEmpty()) {
            items(filteredDrafts, key = { it.id }) { draft ->
                SocialPostCard(
                    language = language,
                    draft = draft,
                    liked = draft.id in likedPostIds,
                    bookmarked = draft.id in bookmarkedPostIds,
                    onShare = { onShareDraft(draft) },
                    onEdit = { onEditDraft(draft) },
                    onDelete = { onDeleteDraft(draft) },
                    onComments = { onOpenComments(draft) },
                    onOpenEvidence = { onOpenEvidence(draft.evidenceUrl) },
                    onOpenMap = { onOpenMap(draft.place) },
                    onReaction = { reaction -> onReaction(draft, reaction) },
                    onToggleBookmark = { onToggleBookmark(draft) }
                )
            }
        }
    }
}

@Composable
private fun FeedHeader(
    language: AppLanguage,
    filter: FeedFilter,
    onFilterChange: (FeedFilter) -> Unit,
    onCreateDraft: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    tr(language, "కాంగ్రెస్ నెట్‌వర్క్", "Congress Network"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    tr(language, "పాలేరు అంతర్గత కార్యకలాపాల ఫీడ్", "Private Paleru activity feed"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                IconButton(onClick = onCreateDraft, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = tr(language, "పోస్ట్ జోడించండి", "Add post"), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StoryChannel(
                    label = tr(language, "కొత్త పోస్ట్", "New post"),
                    icon = Icons.Rounded.Add,
                    selected = false,
                    onClick = onCreateDraft
                )
            }
            item {
                StoryChannel(
                    label = tr(language, "అభివృద్ధి", "Development"),
                    icon = Icons.Rounded.Verified,
                    selected = filter == FeedFilter.DEVELOPMENT,
                    onClick = { onFilterChange(FeedFilter.DEVELOPMENT) }
                )
            }
            item {
                StoryChannel(
                    label = tr(language, "పార్టీ", "Party"),
                    icon = Icons.Rounded.Campaign,
                    selected = filter == FeedFilter.PARTY,
                    onClick = { onFilterChange(FeedFilter.PARTY) }
                )
            }
            item {
                StoryChannel(
                    label = tr(language, "సేవ", "Service"),
                    icon = Icons.Rounded.Groups,
                    selected = filter == FeedFilter.SERVICE,
                    onClick = { onFilterChange(FeedFilter.SERVICE) }
                )
            }
            item {
                StoryChannel(
                    label = tr(language, "సేవ్ చేసినవి", "Saved"),
                    icon = Icons.Rounded.Bookmark,
                    selected = filter == FeedFilter.SAVED,
                    onClick = { onFilterChange(FeedFilter.SAVED) }
                )
            }
        }
    }
}

@Composable
private fun StoryChannel(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(78.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    CircleShape
                )
                .padding(3.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FeedComposer(language: AppLanguage, onCreateDraft: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth().clickable(onClick = onCreateDraft),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Rounded.Campaign, contentDescription = null, modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(tr(language, "ఈ రోజు చేసిన పనిని పంచుకోండి…", "Share today's work…"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(tr(language, "ఫోటో • ప్రదేశం • ఆధారం", "Photo • place • evidence"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.Image, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun InternalFeedNotice(
    language: AppLanguage,
    feedOnline: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (feedOnline) tr(language, "పాలేరు కాంగ్రెస్ సభ్యుల భాగస్వామ్య ఫీడ్", "Shared Paleru Congress member feed")
                    else tr(language, "ఆఫ్‌లైన్ • ఈ ఫోన్‌లోని పోస్ట్‌లు చూపుతున్నాం", "Offline • showing posts saved on this phone"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    tr(language, "కాంగ్రెస్ అంతర్గత వినియోగం మాత్రమే", "For internal Congress use only"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                if (isRefreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.Rounded.Refresh, contentDescription = tr(language, "ఫీడ్ రిఫ్రెష్ చేయండి", "Refresh feed"))
            }
        }
    }
}

@Composable
private fun SocialPostCard(
    language: AppLanguage,
    draft: ActivityPostDraft,
    liked: Boolean,
    bookmarked: Boolean,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComments: () -> Unit,
    onOpenEvidence: () -> Unit,
    onOpenMap: () -> Unit,
    onReaction: (String) -> Unit,
    onToggleBookmark: () -> Unit
) {
    var expanded by rememberSaveable(draft.id) { mutableStateOf(false) }
    var menuOpen by rememberSaveable(draft.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InitialsAvatar(draft.authorName.ifBlank { "Congress" }, tr(language, "రచయిత చిత్రం", "Author portrait"), size = 46)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            draft.authorName.ifBlank { tr(language, "కాంగ్రెస్ సభ్యుడు", "Congress member") },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        "${if (draft.isSynced) tr(language, "సభ్యుడు", "Member") else tr(language, "ఈ ఫోన్‌లో మాత్రమే", "On this phone")} • ${draft.date}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = tr(language, "మరిన్ని ఎంపికలు", "More options"))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (draft.viewerCanEdit) {
                            DropdownMenuItem(
                                text = { Text(tr(language, "పోస్ట్ సవరించండి", "Edit post")) },
                                leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                onClick = { menuOpen = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text(tr(language, "పోస్ట్ తొలగించండి", "Delete post")) },
                                leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
                                onClick = { menuOpen = false; onDelete() }
                            )
                        }
                    }
                }
            }

            if (draft.mediaUri.isNotBlank() || draft.remoteMediaUrl.isNotBlank()) PostMedia(language, draft)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onReaction(if (draft.viewerReaction == "like" || liked) "none" else "like") }) {
                    Icon(Icons.Rounded.ThumbUp, contentDescription = tr(language, "లైక్", "Like"), tint = if (draft.viewerReaction == "like" || liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
                Text(draft.likeCount.toString(), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { onReaction(if (draft.viewerReaction == "dislike") "none" else "dislike") }) {
                    Icon(Icons.Rounded.ThumbDown, contentDescription = tr(language, "డిస్‌లైక్", "Dislike"), tint = if (draft.viewerReaction == "dislike") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
                Text(draft.dislikeCount.toString(), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onComments, enabled = draft.isSynced) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = tr(language, "వ్యాఖ్యలు", "Comments"))
                }
                if (draft.commentCount > 0) Text(draft.commentCount.toString(), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onShare) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = tr(language, "మరొక యాప్ ద్వారా కాపీ పంచుకోండి", "Share a copy through another app"))
                }
                if (draft.evidenceUrl.isNotBlank()) {
                    IconButton(onClick = onOpenEvidence) {
                        Icon(Icons.Rounded.Link, contentDescription = tr(language, "ఆధారం తెరవండి", "Open evidence"))
                    }
                }
                Spacer(Modifier.weight(1f))
                IconToggleButton(checked = bookmarked, onCheckedChange = { onToggleBookmark() }) {
                    Icon(
                        if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (bookmarked) tr(language, "సేవ్ నుంచి తొలగించండి", "Remove from saved") else tr(language, "ఈ పరికరంలో సేవ్ చేయండి", "Save on this device"),
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 16.dp).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(activityCategoryLabel(language, draft), positive = true)
                    if (liked || bookmarked) {
                        Text(
                            tr(language, "ఈ పరికరంలో ${if (liked) "మద్దతు" else "సేవ్"}", "${if (liked) "Supported" else "Saved"} on this device"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    draft.details,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
                )
                if (draft.details.length > 160) {
                    TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                        Text(if (expanded) tr(language, "తక్కువ చూపండి", "Show less") else tr(language, "మరింత", "More"))
                    }
                }
                if (draft.place.isNotBlank()) {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onOpenMap).padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = tr(language, "మ్యాప్స్‌లో ప్రదేశం తెరవండి", "Open place in maps"), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Text(draft.place, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    if (draft.isSynced) tr(language, "సభ్యుల ఫీడ్‌తో సమకాలీకరించబడింది", "Synced with the members feed")
                    else tr(language, "ఈ ఫోన్‌లో మాత్రమే సేవ్ అయింది", "Saved only on this phone"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun PostMedia(language: AppLanguage, draft: ActivityPostDraft) {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        val mediaModel = draft.mediaUri.ifBlank { draft.remoteMediaUrl }
        if (mediaModel.isNotBlank()) {
            SubcomposeAsyncImage(
                model = if (draft.mediaUri.isNotBlank()) Uri.parse(mediaModel) else mediaModel,
                contentDescription = tr(language, "కార్యకలాప ఫోటో", "Activity photo"),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
                error = { PostBrandArtwork(language, draft.title) }
            )
        } else {
            PostBrandArtwork(language, draft.title)
        }
    }
}

@Composable
private fun PostBrandArtwork(language: AppLanguage, title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.congress_hero_ribbons),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.72f)
        ) {
            Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    tr(language, "ఫోటో జోడించని టెక్స్ట్ నవీకరణ", "Text update — no activity photo attached"),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun EmptyFeedCard(language: AppLanguage, savedFilter: Boolean, onCreateDraft: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(if (savedFilter) Icons.Rounded.BookmarkBorder else Icons.Rounded.Campaign, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                if (savedFilter) tr(language, "సేవ్ చేసిన పోస్ట్‌లు లేవు", "No saved posts")
                else tr(language, "ఈ ఛానల్‌లో పోస్ట్‌లు లేవు", "No posts in this channel"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                tr(language, "ఫోటో, ప్రదేశం మరియు ఆధారంతో మొదటి అంతర్గత నవీకరణను సిద్ధం చేయండి.", "Prepare the first internal update with a photo, place and evidence."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!savedFilter) {
                Button(onClick = onCreateDraft, modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(tr(language, "పోస్ట్ సిద్ధం చేయండి", "Create post"))
                }
            }
        }
    }
}

@Composable
private fun CongressNetworkWelcomePost(language: AppLanguage) {
    Card(
        modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column {
            Image(
                painter = painterResource(R.drawable.congress_hero_ribbons),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 7f),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(tr(language, "పిన్ చేసిన అంతర్గత సందేశం", "Pinned internal message"), positive = true)
                Text(tr(language, "పాలేరు కాంగ్రెస్ నెట్‌వర్క్‌కు స్వాగతం", "Welcome to the Paleru Congress Network"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    tr(
                        language,
                        "మండలాలు, గ్రామపంచాయతీలు మరియు పార్టీ బృందాల మధ్య చేసిన పనిని ఆధారాలతో పంచుకోవడానికి ఈ ఫీడ్ రూపొందించబడింది.",
                        "This feed is designed for evidence-backed updates across mandals, Gram Panchayats and Congress teams."
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PostingStandardPost(language: AppLanguage, onCreateDraft: (ContributorRole) -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr(language, "మంచి పోస్ట్‌కు 5 అంశాలు", "The 5-part post standard"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                tr(
                    language,
                    "1. ఎవరు చేశారు  2. ఏ పని జరిగింది  3. ఎక్కడ  4. ఎప్పుడు  5. ఫోటో లేదా ఆధారం",
                    "1. Who led it  2. What happened  3. Where  4. When  5. Photo or evidence"
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedButton(onClick = { onCreateDraft(ContributorRole.PARTY_MEMBER) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(tr(language, "ఈ ప్రమాణంతో పోస్ట్ సిద్ధం చేయండి", "Create a post with this standard"))
            }
        }
    }
}

internal fun roleLabel(language: AppLanguage, role: ContributorRole): String = when (role) {
    ContributorRole.PARTY_MEMBER -> tr(language, "కాంగ్రెస్ సభ్యుడు • ధృవీకరించని డ్రాఫ్ట్ పాత్ర", "Congress member • unverified draft role")
    ContributorRole.SARPANCH -> tr(language, "సర్పంచ్ • ధృవీకరించని డ్రాఫ్ట్ పాత్ర", "Sarpanch • unverified draft role")
    ContributorRole.MANDAL_PRESIDENT -> tr(language, "మండల కాంగ్రెస్ అధ్యక్షుడు • ధృవీకరించని డ్రాఫ్ట్ పాత్ర", "Mandal Congress president • unverified draft role")
}

private fun activityCategoryLabel(language: AppLanguage, draft: ActivityPostDraft): String = when (draft.category) {
    "development" -> tr(language, "అభివృద్ధి", "Development")
    "party-activity" -> tr(language, "పార్టీ కార్యక్రమం", "Party activity")
    "meeting" -> tr(language, "సమావేశం", "Meeting")
    "public-service" -> tr(language, "సేవ", "Service")
    "volunteer" -> tr(language, "వాలంటీర్", "Volunteer")
    "camp" -> tr(language, "శిబిరం", "Camp")
    else -> draft.categoryLabel(language)
}

private fun filterLabel(language: AppLanguage, filter: FeedFilter): String = when (filter) {
    FeedFilter.ALL -> tr(language, "అన్నీ", "All")
    FeedFilter.DEVELOPMENT -> tr(language, "అభివృద్ధి", "Development")
    FeedFilter.PARTY -> tr(language, "పార్టీ", "Party")
    FeedFilter.SERVICE -> tr(language, "సేవ", "Service")
    FeedFilter.CAMPS -> tr(language, "శిబిరాలు", "Camps")
    FeedFilter.SAVED -> tr(language, "సేవ్ చేసినవి", "Saved")
}
