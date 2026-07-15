package com.paleru.congress.ui

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paleru.congress.data.ActivityPostDraft
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.CivicServiceDraft
import com.paleru.congress.data.ContributorRole
import com.paleru.congress.data.DevicePinAccess
import com.paleru.congress.data.DevicePinCopy
import com.paleru.congress.data.DevicePinStage
import com.paleru.congress.data.FeedInteractionStore
import com.paleru.congress.data.LocalDraftStore
import com.paleru.congress.data.PaleruData
import com.paleru.congress.data.PaleruSocialApi
import com.paleru.congress.data.SocialComment
import com.paleru.congress.ui.brand.CongressBackdrop
import com.paleru.congress.ui.brand.CongressBrandMark
import com.paleru.congress.ui.brand.CongressTricolorRail
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private enum class Destination(val icon: ImageVector) {
    DASHBOARD(Icons.Rounded.Home),
    FEED(Icons.Rounded.Campaign),
    WORK(Icons.AutoMirrored.Rounded.Assignment),
    NETWORK(Icons.Rounded.Groups),
    MORE(Icons.Rounded.Menu);

    fun label(language: AppLanguage): String = when (this) {
        DASHBOARD -> tr(language, "హోమ్", "Home")
        FEED -> tr(language, "ఫీడ్", "Feed")
        WORK -> tr(language, "పని", "Work")
        NETWORK -> tr(language, "నెట్‌వర్క్", "Network")
        MORE -> tr(language, "మరిన్ని", "More")
    }
}

private const val PRIVATE_SESSION_TIMEOUT_MS = 5 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaleruCongressApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferences = remember { context.getSharedPreferences("paleru_preferences", Context.MODE_PRIVATE) }
    val access = remember { DevicePinAccess(context) }
    val defaultLanguage = if (Locale.getDefault().language == "te") AppLanguage.TELUGU else AppLanguage.ENGLISH
    var language by remember {
        mutableStateOf(
            preferences.getString("language", null)
                ?.let { stored -> AppLanguage.entries.firstOrNull { it.name == stored } }
                ?: defaultLanguage
        )
    }
    var accessStage by remember { mutableStateOf(access.uiState().stage) }
    var destinationName by rememberSaveable { mutableStateOf(Destination.DASHBOARD.name) }
    val destination = Destination.entries.firstOrNull { it.name == destinationName } ?: Destination.DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val socialApi = remember { PaleruSocialApi(context.applicationContext) }

    var serviceDrafts by remember { mutableStateOf(emptyList<CivicServiceDraft>()) }
    var activityDrafts by remember { mutableStateOf(emptyList<ActivityPostDraft>()) }
    var remotePosts by remember { mutableStateOf(emptyList<ActivityPostDraft>()) }
    var feedOnline by remember { mutableStateOf(false) }
    var feedRefreshing by remember { mutableStateOf(false) }
    var profileCorrections by remember { mutableStateOf(emptyList<com.paleru.congress.data.ProfileCorrectionDraft>()) }
    var likedPostIds by remember { mutableStateOf(emptySet<String>()) }
    var bookmarkedPostIds by remember { mutableStateOf(emptySet<String>()) }
    var serviceDialogOption by remember { mutableStateOf<ServiceOption?>(null) }
    var activityDialogRole by remember { mutableStateOf<ContributorRole?>(null) }
    var activityEditTarget by remember { mutableStateOf<ActivityPostDraft?>(null) }
    var commentsTarget by remember { mutableStateOf<ActivityPostDraft?>(null) }
    var socialComments by remember { mutableStateOf(emptyList<SocialComment>()) }
    var commentsLoading by remember { mutableStateOf(false) }
    var ageUpdateTarget by remember { mutableStateOf<ProfileTarget?>(null) }
    var activityShareTarget by remember { mutableStateOf<ActivityPostDraft?>(null) }
    var serviceShareTarget by remember { mutableStateOf<CivicServiceDraft?>(null) }
    var activityDeleteTarget by remember { mutableStateOf<ActivityPostDraft?>(null) }
    var serviceDeleteTarget by remember { mutableStateOf<CivicServiceDraft?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showResetAccessConfirmation by remember { mutableStateOf(false) }
    var resetWorking by remember { mutableStateOf(false) }
    var privateDataLoaded by remember { mutableStateOf(false) }
    var backgroundedAt by remember { mutableLongStateOf(0L) }

    fun saveLanguage(selected: AppLanguage) {
        language = selected
        preferences.edit().putString("language", selected.name).apply()
    }

    fun reloadPrivateData() {
        serviceDrafts = LocalDraftStore.loadServiceDrafts(context)
        activityDrafts = LocalDraftStore.loadActivityDrafts(context)
        profileCorrections = LocalDraftStore.loadProfileCorrections(context)
        likedPostIds = activityDrafts.filter { FeedInteractionStore.isLiked(context, it.id) }.map { it.id }.toSet()
        bookmarkedPostIds = activityDrafts.filter { FeedInteractionStore.isBookmarked(context, it.id) }.map { it.id }.toSet()
    }

    LaunchedEffect(accessStage) {
        if (accessStage == DevicePinStage.UNLOCKED && !privateDataLoaded) {
            reloadPrivateData()
            privateDataLoaded = true
            feedRefreshing = true
            socialApi.listPosts().fold(
                onSuccess = {
                    remotePosts = it
                    feedOnline = true
                },
                onFailure = { feedOnline = false }
            )
            feedRefreshing = false
        }
    }

    DisposableEffect(lifecycleOwner, accessStage) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> if (accessStage == DevicePinStage.UNLOCKED) {
                    backgroundedAt = SystemClock.elapsedRealtime()
                }

                Lifecycle.Event.ON_START -> if (
                    accessStage == DevicePinStage.UNLOCKED &&
                    backgroundedAt > 0L &&
                    SystemClock.elapsedRealtime() - backgroundedAt >= PRIVATE_SESSION_TIMEOUT_MS
                ) {
                    backgroundedAt = 0L
                    accessStage = access.lockSession().stage
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (accessStage != DevicePinStage.UNLOCKED) {
        PrivateAccessScreen(
            language = language,
            onLanguageChange = ::saveLanguage,
            access = access,
            onUnlocked = {
                accessStage = access.uiState().stage
                reloadPrivateData()
                privateDataLoaded = true
            },
            onLocalDataReset = {
                serviceDrafts = emptyList()
                activityDrafts = emptyList()
                profileCorrections = emptyList()
                likedPostIds = emptySet()
                bookmarkedPostIds = emptySet()
                privateDataLoaded = false
                accessStage = access.uiState().stage
            }
        )
        return
    }

    fun message(te: String, en: String) {
        scope.launch { snackbarHostState.showSnackbar(tr(language, te, en)) }
    }

    fun refreshSocialFeed(showFailure: Boolean = true) {
        if (feedRefreshing) return
        feedRefreshing = true
        scope.launch {
            socialApi.listPosts().fold(
                onSuccess = {
                    remotePosts = it
                    feedOnline = true
                },
                onFailure = {
                    feedOnline = false
                    if (showFailure) message("ఫీడ్‌ను చేరుకోలేకపోయాం. స్థానిక పోస్ట్‌లు చూపిస్తున్నాం.", "The shared feed is unavailable. Showing posts saved on this phone.")
                }
            )
            feedRefreshing = false
        }
    }

    fun launchMap(query: String) {
        if (!openMap(context, query)) message("మ్యాప్ తెరవలేకపోయాం.", "No maps app or browser could be opened.")
    }

    fun launchUrl(url: String) {
        if (!openUrl(context, url)) message("లింక్ తెరవలేకపోయాం.", "No browser could open this link.")
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 720.dp
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    CongressTricolorRail(modifier = Modifier.fillMaxWidth(), height = 5.dp)
                    TopAppBar(
                        title = {
                            Row {
                                CongressBrandMark(size = 38.dp, contentDescription = null)
                                Column(modifier = Modifier.padding(start = 9.dp)) {
                                    Text(tr(language, "పాలేరు కాంగ్రెస్", "Paleru Congress"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                    Text(tr(language, "అంతర్గతం", "PRIVATE"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        actions = {
                            TextButton(onClick = { saveLanguage(if (language == AppLanguage.TELUGU) AppLanguage.ENGLISH else AppLanguage.TELUGU) }) {
                                Text(if (language == AppLanguage.TELUGU) "English" else "తెలుగు", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                accessStage = access.lockSession().stage
                            }) {
                                Icon(Icons.Rounded.Lock, contentDescription = tr(language, "యాప్ లాక్", "Lock app"))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        Destination.entries.forEach { item ->
                            NavigationBarItem(
                                selected = destination == item,
                                onClick = { destinationName = item.name },
                                icon = { Icon(item.icon, contentDescription = item.label(language)) },
                                label = { Text(item.label(language), maxLines = 1) }
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (useNavigationRail) {
                    NavigationRail(
                        modifier = Modifier.width(104.dp).padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Destination.entries.forEach { item ->
                            NavigationRailItem(
                                selected = destination == item,
                                onClick = { destinationName = item.name },
                                icon = { Icon(item.icon, contentDescription = item.label(language)) },
                                label = { Text(item.label(language), maxLines = 1) }
                            )
                        }
                    }
                }
                CongressBackdrop(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (destination) {
                        Destination.DASHBOARD -> HomeScreen(
                            language = language,
                            pendingFieldDrafts = serviceDrafts.size,
                            pendingActivityDrafts = activityDrafts.size,
                            pendingProfileUpdates = profileCorrections.size,
                            onNewPost = { activityDialogRole = ContributorRole.PARTY_MEMBER },
                            onAddFieldIssue = { serviceDialogOption = serviceOptions.first { it.id == "other" } },
                            onOpenDirectory = { destinationName = Destination.NETWORK.name },
                            onOpenFeed = { destinationName = Destination.FEED.name },
                            onOpenConstituencyMap = { launchMap("Palair Assembly Constituency, Khammam, Telangana, India") },
                            onOpenLeaderSource = { launchUrl(PaleruData.currentLeader.photoSourceUrl) },
                            onUpdateLeaderAge = {
                                val leader = PaleruData.currentLeader
                                ageUpdateTarget = ProfileTarget(
                                    directoryId = "mla-palair-2023",
                                    personName = leader.name.display(language),
                                    role = leader.role.display(language),
                                    place = leader.constituency.display(language),
                                    currentAge = leader.declaredAge,
                                    ageReferenceYear = leader.ageReferenceYear
                                )
                            },
                            contentPadding = innerPadding
                        )

                        Destination.FEED -> ActivitiesScreen(
                            language = language,
                            drafts = remotePosts + activityDrafts,
                            likedPostIds = likedPostIds,
                            bookmarkedPostIds = bookmarkedPostIds,
                            feedOnline = feedOnline,
                            isRefreshing = feedRefreshing,
                            onCreateDraft = { activityDialogRole = it },
                            onShareDraft = { draft -> activityShareTarget = draft },
                            onEditDraft = { draft -> activityEditTarget = draft },
                            onDeleteDraft = { draft -> activityDeleteTarget = draft },
                            onOpenComments = { draft ->
                                if (draft.remoteId.isBlank()) {
                                    message("వ్యాఖ్యలకు ఇంటర్నెట్ సమకాలీకరణ అవసరం.", "Comments require a synced post.")
                                } else {
                                    commentsTarget = draft
                                    commentsLoading = true
                                    scope.launch {
                                        socialApi.listComments(draft.remoteId).fold(
                                            onSuccess = { socialComments = it },
                                            onFailure = { message("వ్యాఖ్యలను లోడ్ చేయలేకపోయాం.", "Comments could not be loaded.") }
                                        )
                                        commentsLoading = false
                                    }
                                }
                            },
                            onRefresh = { refreshSocialFeed() },
                            onOpenEvidence = ::launchUrl,
                            onOpenMap = ::launchMap,
                            onReaction = { draft, reaction ->
                                if (draft.remoteId.isNotBlank()) {
                                    scope.launch {
                                        socialApi.react(draft.remoteId, reaction).fold(
                                            onSuccess = { updated -> remotePosts = remotePosts.map { if (it.remoteId == updated.remoteId) updated else it } },
                                            onFailure = { message("ప్రతిస్పందన సేవ్ కాలేదు.", "The reaction could not be saved.") }
                                        )
                                    }
                                } else if (reaction != "dislike") {
                                    val active = FeedInteractionStore.toggleLiked(context, draft.id)
                                    likedPostIds = if (active) likedPostIds + draft.id else likedPostIds - draft.id
                                } else {
                                    message("డిస్‌లైక్ కోసం పోస్ట్ ముందుగా సమకాలీకరించాలి.", "Sync the post before using dislike.")
                                }
                            },
                            onToggleBookmark = { draft ->
                                val active = FeedInteractionStore.toggleBookmarked(context, draft.id)
                                bookmarkedPostIds = if (active) bookmarkedPostIds + draft.id else bookmarkedPostIds - draft.id
                            },
                            contentPadding = innerPadding
                        )

                        Destination.WORK -> ServicesScreen(
                            language = language,
                            drafts = serviceDrafts,
                            onCreateDraft = { serviceDialogOption = it },
                            onShareDraft = { draft -> serviceShareTarget = draft },
                            onOpenMap = ::launchMap,
                            onDeleteDraft = { draft -> serviceDeleteTarget = draft },
                            contentPadding = innerPadding
                        )

                        Destination.NETWORK -> DirectoryScreen(
                            language = language,
                            onOpenMap = ::launchMap,
                            onOpenSource = ::launchUrl,
                            onUpdateAge = { ageUpdateTarget = it },
                            onContributeCorrection = { person, place ->
                                val body = tr(
                                    language,
                                    "పాలేరు కాంగ్రెస్ ప్రొఫైల్ సవరణ\nపేరు: $person\nప్రదేశం: $place\n\nగుర్తింపు ఆధారం, ఫోటో అనుమతి మరియు వనరు జోడించండి.",
                                    "Paleru Congress profile correction\nPerson: $person\nPlace: $place\n\nAttach identity evidence, photo permission and source."
                                )
                                if (!shareText(context, tr(language, "ప్రొఫైల్ సవరణ", "Profile correction"), body)) {
                                    message("పంచుకునే యాప్ దొరకలేదు.", "No sharing app was found.")
                                }
                            },
                            contentPadding = innerPadding
                        )

                        Destination.MORE -> MoreScreen(
                            language = language,
                            pendingProfileCorrections = profileCorrections.size,
                            onOpenSource = ::launchUrl,
                            onClearLocalData = { showClearConfirmation = true },
                            onLockApp = { accessStage = access.lockSession().stage },
                            onResetAccess = { showResetAccessConfirmation = true },
                            contentPadding = innerPadding
                        )
                    }
                }
            }
        }
    }

    serviceDialogOption?.let { option ->
        ServiceDraftDialog(
            language = language,
            initialService = option,
            onDismiss = { serviceDialogOption = null },
            onSave = { draft ->
                if (LocalDraftStore.saveServiceDraft(context, draft) != null) {
                    serviceDrafts = LocalDraftStore.loadServiceDrafts(context)
                    serviceDialogOption = null
                    message("అంతర్గత ఫీల్డ్ నోట్ పరికరంలో సేవ్ అయింది.", "Internal field note saved on this device.")
                } else {
                    message("ఫీల్డ్ నోట్ సేవ్ కాలేదు. మళ్లీ ప్రయత్నించండి.", "The field note was not saved. Try again.")
                }
            }
        )
    }

    activityDialogRole?.let { role ->
        ActivityDraftDialog(
            language = language,
            initialRole = role,
            onDismiss = { activityDialogRole = null },
            onSave = { draft ->
                activityDialogRole = null
                destinationName = Destination.FEED.name
                message("పోస్ట్ చేస్తున్నాం…", "Posting…")
                scope.launch {
                    socialApi.createPost(draft).fold(
                        onSuccess = { posted ->
                            remotePosts = listOf(posted) + remotePosts.filterNot { it.remoteId == posted.remoteId }
                            feedOnline = true
                            deleteOwnedActivityImage(context, draft.mediaUri)
                            message("పోస్ట్ సభ్యుల ఫీడ్‌లో ప్రచురించబడింది.", "Posted to the members feed.")
                        },
                        onFailure = {
                            if (LocalDraftStore.saveActivityDraft(context, draft) != null) {
                                activityDrafts = LocalDraftStore.loadActivityDrafts(context)
                                feedOnline = false
                                message("నెట్‌వర్క్ అందుబాటులో లేదు. పోస్ట్ ఈ ఫోన్‌లో సేవ్ అయింది.", "Network unavailable. The post was saved on this phone and can be shared later.")
                            } else {
                                message("పోస్ట్ సేవ్ కాలేదు. మళ్లీ ప్రయత్నించండి.", "The post could not be saved. Try again.")
                            }
                        }
                    )
                }
            }
        )
    }

    activityEditTarget?.let { draft ->
        ActivityDraftDialog(
            language = language,
            initialRole = draft.role,
            initialDraft = draft,
            onDismiss = { activityEditTarget = null },
            onSave = { updated ->
                activityEditTarget = null
                if (updated.remoteId.isNotBlank()) {
                    scope.launch {
                        socialApi.updatePost(updated).fold(
                            onSuccess = { saved ->
                                remotePosts = remotePosts.map { if (it.remoteId == saved.remoteId) saved else it }
                                message("పోస్ట్ నవీకరించబడింది.", "Post updated.")
                            },
                            onFailure = { message("పోస్ట్ నవీకరించబడలేదు.", "The post could not be updated.") }
                        )
                    }
                } else if (LocalDraftStore.saveActivityDraft(context, updated) != null) {
                    activityDrafts = LocalDraftStore.loadActivityDrafts(context)
                    message("స్థానిక పోస్ట్ నవీకరించబడింది.", "Local post updated.")
                }
            }
        )
    }

    ageUpdateTarget?.let { target ->
        AgeUpdateDialog(
            language = language,
            target = target,
            onDismiss = { ageUpdateTarget = null },
            onSave = { correction ->
                if (LocalDraftStore.saveProfileCorrection(context, correction) != null) {
                    profileCorrections = LocalDraftStore.loadProfileCorrections(context)
                    ageUpdateTarget = null
                    message("వయస్సు సవరణ పరికరంలో సేవ్ అయింది; ఇంకా సమర్పించలేదు.", "Age correction saved on this device; it has not been submitted.")
                } else {
                    message("వయస్సు సవరణ సేవ్ కాలేదు. మళ్లీ ప్రయత్నించండి.", "The age suggestion was not saved. Try again.")
                }
            }
        )
    }

    activityShareTarget?.let { draft ->
        AlertDialog(
            onDismissRequest = { activityShareTarget = null },
            title = { Text(tr(language, "యాప్ వెలుపల కాపీ పంచుకోవాలా?", "Share a copy outside the app?")) },
            text = {
                Text(
                    tr(
                        language,
                        "Android Share ఈ అంతర్గత డ్రాఫ్ట్, రచయిత పేరు, ఆధారం మరియు జోడించిన ఫోటోను మీరు ఎంచుకున్న మరొక యాప్‌కు పంపుతుంది. ఆమోదిత వ్యక్తిని మాత్రమే ఎంచుకోండి.",
                        "Android Share sends this internal draft, author name, evidence and any attached photo to another app you choose. Select only an approved recipient."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    activityShareTarget = null
                    if (!shareActivityDraft(context, language, draft)) {
                        message("పంచుకునే యాప్ లేదా ఫోటో అనుమతి అందుబాటులో లేదు.", "No sharing app or photo permission is available.")
                    }
                }) { Text(tr(language, "షేర్ షీట్ తెరవండి", "Open share sheet")) }
            },
            dismissButton = { TextButton(onClick = { activityShareTarget = null }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }

    serviceShareTarget?.let { draft ->
        AlertDialog(
            onDismissRequest = { serviceShareTarget = null },
            title = { Text(tr(language, "ఫీల్డ్ నోట్ కాపీ పంచుకోవాలా?", "Share a field-note copy?")) },
            text = {
                Text(
                    tr(
                        language,
                        "ఇది యాప్ వెలుపలికి వెళుతుంది. పేరు మరియు ఫోన్ నంబర్ ఆటోమేటిక్‌గా తీసివేయబడ్డాయి; అయినా వివరాలను చూసి ఆమోదిత వ్యక్తినే ఎంచుకోండి.",
                        "This leaves the private app. Name and phone are automatically omitted; still review the details and choose only an approved recipient."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    serviceShareTarget = null
                    if (!shareServiceDraft(context, language, draft)) {
                        message("పంచుకునే యాప్ దొరకలేదు.", "No sharing app was found.")
                    }
                }) { Text(tr(language, "షేర్ షీట్ తెరవండి", "Open share sheet")) }
            },
            dismissButton = { TextButton(onClick = { serviceShareTarget = null }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }

    activityDeleteTarget?.let { draft ->
        AlertDialog(
            onDismissRequest = { activityDeleteTarget = null },
            title = { Text(tr(language, "పోస్ట్ తొలగించాలా?", "Delete post?")) },
            text = { Text(if (draft.isSynced) tr(language, "ఈ పోస్ట్ మరియు దాని వ్యాఖ్యలు సభ్యుల ఫీడ్ నుంచి తొలగించబడతాయి.", "This post and its comments will be removed from the members feed.") else tr(language, "ఈ పోస్ట్ ఈ పరికరం నుంచి శాశ్వతంగా తొలగించబడుతుంది.", "This post will be permanently removed from this device.")) },
            confirmButton = {
                TextButton(onClick = {
                    activityDeleteTarget = null
                    if (draft.isSynced) {
                        scope.launch {
                            socialApi.deletePost(draft.remoteId).fold(
                                onSuccess = {
                                    remotePosts = remotePosts.filterNot { it.remoteId == draft.remoteId }
                                    message("పోస్ట్ తొలగించబడింది.", "Post deleted.")
                                },
                                onFailure = { message("పోస్ట్ తొలగించబడలేదు.", "The post could not be deleted.") }
                            )
                        }
                    } else {
                        val deleted = LocalDraftStore.deleteActivityDraft(context, draft.id)
                        if (deleted) {
                            deleteOwnedActivityImage(context, draft.mediaUri)
                            FeedInteractionStore.removePost(context, draft.id)
                            activityDrafts = LocalDraftStore.loadActivityDrafts(context)
                            likedPostIds = likedPostIds - draft.id
                            bookmarkedPostIds = bookmarkedPostIds - draft.id
                            message("స్థానిక పోస్ట్ తొలగించబడింది.", "Local post deleted.")
                        } else {
                            message("పోస్ట్ తొలగించబడలేదు. మళ్లీ ప్రయత్నించండి.", "The post was not deleted. Try again.")
                        }
                    }
                }) { Text(tr(language, "తొలగించు", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { activityDeleteTarget = null }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }

    commentsTarget?.let { post ->
        CommentsDialog(
            language = language,
            comments = socialComments,
            loading = commentsLoading,
            onDismiss = {
                commentsTarget = null
                socialComments = emptyList()
            },
            onSend = { content ->
                val profileName = context.getSharedPreferences("paleru_post_profile", Context.MODE_PRIVATE)
                    .getString("author_name", null).orEmpty().ifBlank { "Congress member" }
                commentsLoading = true
                scope.launch {
                    socialApi.addComment(post.remoteId, profileName, content).fold(
                        onSuccess = { comment ->
                            socialComments = socialComments + comment
                            remotePosts = remotePosts.map { if (it.remoteId == post.remoteId) it.copy(commentCount = it.commentCount + 1) else it }
                        },
                        onFailure = { message("వ్యాఖ్య పంపబడలేదు.", "The comment could not be posted.") }
                    )
                    commentsLoading = false
                }
            },
            onDelete = { comment ->
                scope.launch {
                    socialApi.deleteComment(post.remoteId, comment.id).fold(
                        onSuccess = {
                            socialComments = socialComments.filterNot { it.id == comment.id }
                            remotePosts = remotePosts.map { if (it.remoteId == post.remoteId) it.copy(commentCount = maxOf(0, it.commentCount - 1)) else it }
                        },
                        onFailure = { message("వ్యాఖ్య తొలగించబడలేదు.", "The comment could not be deleted.") }
                    )
                }
            }
        )
    }

    serviceDeleteTarget?.let { draft ->
        AlertDialog(
            onDismissRequest = { serviceDeleteTarget = null },
            title = { Text(tr(language, "ఫీల్డ్ నోట్ తొలగించాలా?", "Delete field note?")) },
            text = { Text(tr(language, "ఈ అంతర్గత నోట్ ఈ పరికరం నుంచి శాశ్వతంగా తొలగించబడుతుంది.", "This internal note will be permanently removed from this device.")) },
            confirmButton = {
                TextButton(onClick = {
                    if (LocalDraftStore.deleteServiceDraft(context, draft.id)) {
                        serviceDrafts = LocalDraftStore.loadServiceDrafts(context)
                        message("ఫీల్డ్ నోట్ తొలగించబడింది.", "Field note deleted.")
                    } else {
                        message("ఫీల్డ్ నోట్ తొలగించబడలేదు. మళ్లీ ప్రయత్నించండి.", "The field note was not deleted. Try again.")
                    }
                    serviceDeleteTarget = null
                }) { Text(tr(language, "తొలగించు", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { serviceDeleteTarget = null }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(tr(language, "స్థానిక ముసాయిదాలు తొలగించాలా?", "Delete local drafts?")) },
            text = { Text(tr(language, "అంతర్గత పోస్ట్‌లు, ఫీల్డ్ నోట్‌లు, సేవ్‌లు మరియు ప్రొఫైల్ సవరణలు ఈ పరికరం నుంచి తొలగించబడతాయి.", "Internal posts, field notes, saves and profile corrections will be removed from this device.")) },
            confirmButton = {
                TextButton(onClick = {
                    val retainedMediaCleared = activityDrafts
                        .map(ActivityPostDraft::mediaUri)
                        .filter(String::isNotBlank)
                        .all { deleteOwnedActivityImage(context, it) }
                    val draftsCleared = LocalDraftStore.clearAll(context)
                    val interactionsCleared = FeedInteractionStore.clearAll(context)
                    val mediaCleared = deleteAllOwnedActivityImages(context)
                    showClearConfirmation = false
                    if (retainedMediaCleared && draftsCleared && interactionsCleared && mediaCleared) {
                        serviceDrafts = emptyList()
                        activityDrafts = emptyList()
                        profileCorrections = emptyList()
                        likedPostIds = emptySet()
                        bookmarkedPostIds = emptySet()
                        message("స్థానిక డేటా తొలగించబడింది.", "Local data deleted.")
                    } else {
                        reloadPrivateData()
                        message("కొంత స్థానిక డేటా తొలగించబడలేదు. మళ్లీ ప్రయత్నించండి.", "Some local data could not be deleted. Try again.")
                    }
                }) { Text(tr(language, "తొలగించు", "Delete")) }
            },
            dismissButton = { TextButton(onClick = { showClearConfirmation = false }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }

    if (showResetAccessConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!resetWorking) showResetAccessConfirmation = false },
            title = { Text(tr(language, "PIN రీసెట్ చేసి అన్నీ తొలగించాలా?", "Reset PIN and delete everything?")) },
            text = { Text(DevicePinCopy.resetWarning.inLanguage(language)) },
            confirmButton = {
                TextButton(onClick = {
                    if (!resetWorking) {
                        scope.launch {
                            resetWorking = true
                            val result = withContext(Dispatchers.Default) { access.resetPinAndWipeAllLocalData() }
                            resetWorking = false
                            showResetAccessConfirmation = false
                            if (result.succeeded) {
                                serviceDrafts = emptyList()
                                activityDrafts = emptyList()
                                profileCorrections = emptyList()
                                likedPostIds = emptySet()
                                bookmarkedPostIds = emptySet()
                                privateDataLoaded = false
                                accessStage = result.nextState.stage
                            } else {
                                message(result.message.te, result.message.en)
                            }
                        }
                    }
                }) { Text(tr(language, "అన్నీ తొలగించి రీసెట్", "Delete all and reset"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showResetAccessConfirmation = false }) { Text(tr(language, "రద్దు", "Cancel")) } }
        )
    }
}

private fun shareServiceDraft(context: Context, language: AppLanguage, draft: CivicServiceDraft): Boolean {
    val body = tr(
        language,
        "పాలేరు కాంగ్రెస్ అంతర్గత ఫీల్డ్ నోట్\nరిఫరెన్స్: ${draft.id}\nవర్గం: ${draft.categoryTe}\nమండలం: ${draft.mandal}\nగ్రామం: ${draft.village}\nవివరాలు: ${draft.details}\n\nగోప్యత కోసం పేరు మరియు ఫోన్ తొలగించబడ్డాయి.",
        "Paleru Congress internal field note\nReference: ${draft.id}\nCategory: ${draft.categoryEn}\nMandal: ${draft.mandal}\nVillage: ${draft.village}\nDetails: ${draft.details}\n\nName and phone omitted for privacy."
    )
    return shareText(context, tr(language, "అంతర్గత ఫీల్డ్ నోట్", "Internal field note"), body)
}

private fun shareActivityDraft(context: Context, language: AppLanguage, draft: ActivityPostDraft): Boolean {
    val body = tr(
        language,
        "పాలేరు కాంగ్రెస్ కార్యకలాపం\n${draft.title}\n${roleLabel(language, draft.role)} • ${draft.authorName}\n${draft.place} • ${draft.date}\n\n${draft.details}\n\nఆధారం: ${draft.evidenceUrl}\nఅంతర్గత ముసాయిదా • ${draft.id}",
        "Paleru Congress activity\n${draft.title}\n${roleLabel(language, draft.role)} • ${draft.authorName}\n${draft.place} • ${draft.date}\n\n${draft.details}\n\nEvidence: ${draft.evidenceUrl}\nInternal draft • ${draft.id}"
    )
    return if (draft.mediaUri.isNotBlank()) {
        shareMediaText(context, Uri.parse(draft.mediaUri), tr(language, "కాంగ్రెస్ కార్యకలాపం", "Congress activity"), body)
    } else {
        shareText(context, tr(language, "కాంగ్రెస్ కార్యకలాపం", "Congress activity"), body)
    }
}
