package com.paleru.congress.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * A role claimed by the author of an activity draft.
 *
 * Selecting a role is not proof of identity or publishing permission. A server-backed
 * verification flow must establish that before an activity can become public.
 */
enum class ContributorRole {
    PARTY_MEMBER,
    SARPANCH,
    MANDAL_PRESIDENT;

    fun label(language: AppLanguage): String = when (this) {
        PARTY_MEMBER -> if (language == AppLanguage.TELUGU) {
            "కాంగ్రెస్ పార్టీ సభ్యుడు"
        } else {
            "Congress party member"
        }

        SARPANCH -> if (language == AppLanguage.TELUGU) "సర్పంచ్" else "Sarpanch"
        MANDAL_PRESIDENT -> if (language == AppLanguage.TELUGU) {
            "మండల కాంగ్రెస్ అధ్యక్షుడు"
        } else {
            "Mandal Congress president"
        }
    }
}

/**
 * These are deliberately the only states available to offline data.
 * Neither state means that a record was uploaded, approved, or published.
 */
enum class LocalDraftStatus {
    LOCAL_ONLY,
    PENDING_VERIFICATION;

    fun label(language: AppLanguage): String = when (this) {
        LOCAL_ONLY -> if (language == AppLanguage.TELUGU) {
            "ఈ పరికరంలో మాత్రమే భద్రపరచబడింది"
        } else {
            "Saved only on this device"
        }

        PENDING_VERIFICATION -> if (language == AppLanguage.TELUGU) {
            "ధృవీకరణ కోసం వేచి ఉంది"
        } else {
            "Pending verification"
        }
    }
}

data class CivicServiceDraft(
    val id: String = DraftIdGenerator.newId("PLR"),
    val category: String = "",
    val categoryTe: String = "",
    val categoryEn: String = "",
    val mandal: String = "",
    val village: String = "",
    val name: String = "",
    val phone: String = "",
    val details: String = "",
    val language: AppLanguage = AppLanguage.TELUGU,
    val status: LocalDraftStatus = LocalDraftStatus.LOCAL_ONLY,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = createdAtEpochMillis
) {
    fun categoryLabel(displayLanguage: AppLanguage): String {
        val localized = if (displayLanguage == AppLanguage.TELUGU) categoryTe else categoryEn
        return localized.ifBlank { category }
    }

    fun placeLabel(): String = listOf(village, mandal)
        .filter(String::isNotBlank)
        .joinToString(", ")
}

data class ActivityPostDraft(
    val id: String = DraftIdGenerator.newId("ACT"),
    val title: String = "",
    val details: String = "",
    val category: String = "",
    val categoryTe: String = "",
    val categoryEn: String = "",
    val place: String = "",
    val date: String = "",
    val evidenceUrl: String = "",
    val mediaUri: String = "",
    val mediaConsentConfirmed: Boolean = false,
    val authorName: String = "",
    val role: ContributorRole = ContributorRole.PARTY_MEMBER,
    val authorDirectoryId: String = "",
    val language: AppLanguage = AppLanguage.TELUGU,
    val status: LocalDraftStatus = LocalDraftStatus.LOCAL_ONLY,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = createdAtEpochMillis,
    val remoteId: String = "",
    val remoteMediaUrl: String = "",
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val commentCount: Int = 0,
    val viewerReaction: String = "none",
    val viewerCanEdit: Boolean = true
) {
    fun categoryLabel(displayLanguage: AppLanguage): String {
        val localized = if (displayLanguage == AppLanguage.TELUGU) categoryTe else categoryEn
        return localized.ifBlank { category }
    }

    val isSynced: Boolean get() = remoteId.isNotBlank()
}

/**
 * A locally stored request to correct a public directory profile.
 *
 * Age values are proposals backed by a reference year and evidence URL. They remain
 * pending verification and must not replace verified public data merely by being saved.
 */
data class ProfileCorrectionDraft(
    val id: String = DraftIdGenerator.newId("COR"),
    val directoryId: String = "",
    val personName: String = "",
    val role: String = "",
    val place: String = "",
    val proposedAge: Int? = null,
    val ageReferenceYear: Int? = null,
    val evidenceUrl: String = "",
    val submitterName: String = "",
    val notes: String = "",
    val language: AppLanguage = AppLanguage.TELUGU,
    val status: LocalDraftStatus = LocalDraftStatus.PENDING_VERIFICATION,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = createdAtEpochMillis
)

/**
 * Synchronous, small-data persistence for drafts that remain on this device.
 *
 * `saveServiceDraft` and `saveActivityDraft` are upserts. They generate an ASCII ID
 * when needed and normalize every decimal numeral to 0-9 before persistence.
 */
object LocalDraftStore {
    private const val preferencesName = "paleru_local_drafts_v1"
    private const val serviceDraftsKey = "service_drafts"
    private const val activityDraftsKey = "activity_drafts"
    private const val profileCorrectionsKey = "profile_corrections"

    @Synchronized
    fun loadServiceDrafts(context: Context): List<CivicServiceDraft> =
        readArray(context, serviceDraftsKey)
            .mapNotNull(::serviceDraftFromJson)
            .sortedByDescending(CivicServiceDraft::updatedAtEpochMillis)

    @Synchronized
    fun findServiceDraft(context: Context, id: String): CivicServiceDraft? {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        return loadServiceDrafts(context).firstOrNull { it.id == normalizedId }
    }

    @Synchronized
    fun saveServiceDraft(context: Context, draft: CivicServiceDraft): CivicServiceDraft? {
        val now = System.currentTimeMillis()
        val normalized = draft.normalized(now)
        val drafts = loadServiceDrafts(context).toMutableList()
        val existingIndex = drafts.indexOfFirst { it.id == normalized.id }
        if (existingIndex >= 0) drafts[existingIndex] = normalized else drafts.add(normalized)
        return normalized.takeIf { writeArray(context, serviceDraftsKey, drafts.map(::serviceDraftToJson)) }
    }

    @Synchronized
    fun deleteServiceDraft(context: Context, id: String): Boolean {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        val drafts = loadServiceDrafts(context).toMutableList()
        val removed = drafts.removeAll { it.id == normalizedId }
        if (!removed) return false
        return writeArray(context, serviceDraftsKey, drafts.map(::serviceDraftToJson))
    }

    @Synchronized
    fun clearServiceDrafts(context: Context): Boolean =
        preferences(context).edit().remove(serviceDraftsKey).commit()

    @Synchronized
    fun loadActivityDrafts(context: Context): List<ActivityPostDraft> =
        readArray(context, activityDraftsKey)
            .mapNotNull(::activityDraftFromJson)
            .sortedByDescending(ActivityPostDraft::updatedAtEpochMillis)

    @Synchronized
    fun findActivityDraft(context: Context, id: String): ActivityPostDraft? {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        return loadActivityDrafts(context).firstOrNull { it.id == normalizedId }
    }

    @Synchronized
    fun saveActivityDraft(context: Context, draft: ActivityPostDraft): ActivityPostDraft? {
        val now = System.currentTimeMillis()
        val normalized = draft.normalized(now)
        val drafts = loadActivityDrafts(context).toMutableList()
        val existingIndex = drafts.indexOfFirst { it.id == normalized.id }
        if (existingIndex >= 0) drafts[existingIndex] = normalized else drafts.add(normalized)
        return normalized.takeIf { writeArray(context, activityDraftsKey, drafts.map(::activityDraftToJson)) }
    }

    @Synchronized
    fun deleteActivityDraft(context: Context, id: String): Boolean {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        val drafts = loadActivityDrafts(context).toMutableList()
        val removed = drafts.removeAll { it.id == normalizedId }
        if (!removed) return false
        return writeArray(context, activityDraftsKey, drafts.map(::activityDraftToJson))
    }

    @Synchronized
    fun clearActivityDrafts(context: Context): Boolean =
        preferences(context).edit().remove(activityDraftsKey).commit()

    @Synchronized
    fun loadProfileCorrections(context: Context): List<ProfileCorrectionDraft> =
        readArray(context, profileCorrectionsKey)
            .mapNotNull(::profileCorrectionFromJson)
            .sortedByDescending(ProfileCorrectionDraft::updatedAtEpochMillis)

    @Synchronized
    fun findProfileCorrection(context: Context, id: String): ProfileCorrectionDraft? {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        return loadProfileCorrections(context).firstOrNull { it.id == normalizedId }
    }

    @Synchronized
    fun saveProfileCorrection(
        context: Context,
        draft: ProfileCorrectionDraft
    ): ProfileCorrectionDraft? {
        val now = System.currentTimeMillis()
        val normalized = draft.normalized(now)
        val drafts = loadProfileCorrections(context).toMutableList()
        val existingIndex = drafts.indexOfFirst { it.id == normalized.id }
        if (existingIndex >= 0) drafts[existingIndex] = normalized else drafts.add(normalized)
        return normalized.takeIf { writeArray(context, profileCorrectionsKey, drafts.map(::profileCorrectionToJson)) }
    }

    @Synchronized
    fun deleteProfileCorrection(context: Context, id: String): Boolean {
        val normalizedId = DraftIdGenerator.normalizeExisting(id)
        val drafts = loadProfileCorrections(context).toMutableList()
        val removed = drafts.removeAll { it.id == normalizedId }
        if (!removed) return false
        return writeArray(context, profileCorrectionsKey, drafts.map(::profileCorrectionToJson))
    }

    @Synchronized
    fun clearProfileCorrections(context: Context): Boolean =
        preferences(context).edit().remove(profileCorrectionsKey).commit()

    @Synchronized
    fun clearAll(context: Context): Boolean = preferences(context)
        .edit()
        .remove(serviceDraftsKey)
        .remove(activityDraftsKey)
        .remove(profileCorrectionsKey)
        .commit()

    private fun preferences(context: Context) = context.applicationContext.getSharedPreferences(
        preferencesName,
        Context.MODE_PRIVATE
    )

    private fun readArray(context: Context, key: String): List<JSONObject> {
        val raw = preferences(context).getString(key, null) ?: return emptyList()
        val array = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return emptyList()
        }
        return buildList {
            for (index in 0 until array.length()) {
                array.optJSONObject(index)?.let(::add)
            }
        }
    }

    private fun writeArray(
        context: Context,
        key: String,
        objects: List<JSONObject>
    ): Boolean {
        val array = JSONArray()
        objects.forEach(array::put)
        return preferences(context).edit().putString(key, array.toString()).commit()
    }

    private fun serviceDraftToJson(draft: CivicServiceDraft) = JSONObject().apply {
        put("id", draft.id)
        put("category", draft.category)
        put("categoryTe", draft.categoryTe)
        put("categoryEn", draft.categoryEn)
        put("mandal", draft.mandal)
        put("village", draft.village)
        put("name", draft.name)
        put("phone", draft.phone)
        put("details", draft.details)
        put("language", draft.language.name)
        put("status", draft.status.name)
        put("createdAtEpochMillis", draft.createdAtEpochMillis)
        put("updatedAtEpochMillis", draft.updatedAtEpochMillis)
    }

    private fun serviceDraftFromJson(json: JSONObject): CivicServiceDraft? = try {
        val now = System.currentTimeMillis()
        CivicServiceDraft(
            id = json.optString("id"),
            category = json.optString("category"),
            categoryTe = json.optString("categoryTe"),
            categoryEn = json.optString("categoryEn"),
            mandal = json.optString("mandal"),
            village = json.optString("village"),
            name = json.optString("name"),
            phone = json.optString("phone"),
            details = json.optString("details"),
            language = enumValueOrDefault(json.optString("language"), AppLanguage.TELUGU),
            status = enumValueOrDefault(
                json.optString("status"),
                LocalDraftStatus.LOCAL_ONLY
            ),
            createdAtEpochMillis = json.optLong("createdAtEpochMillis", now),
            updatedAtEpochMillis = json.optLong("updatedAtEpochMillis", now)
        ).normalizedFromDisk()
    } catch (_: Exception) {
        null
    }

    private fun activityDraftToJson(draft: ActivityPostDraft) = JSONObject().apply {
        put("id", draft.id)
        put("title", draft.title)
        put("details", draft.details)
        put("category", draft.category)
        put("categoryTe", draft.categoryTe)
        put("categoryEn", draft.categoryEn)
        put("place", draft.place)
        put("date", draft.date)
        put("evidenceUrl", draft.evidenceUrl)
        put("mediaUri", draft.mediaUri)
        put("mediaConsentConfirmed", draft.mediaConsentConfirmed)
        put("authorName", draft.authorName)
        put("role", draft.role.name)
        put("authorDirectoryId", draft.authorDirectoryId)
        put("language", draft.language.name)
        put("status", draft.status.name)
        put("createdAtEpochMillis", draft.createdAtEpochMillis)
        put("updatedAtEpochMillis", draft.updatedAtEpochMillis)
    }

    private fun activityDraftFromJson(json: JSONObject): ActivityPostDraft? = try {
        val now = System.currentTimeMillis()
        ActivityPostDraft(
            id = json.optString("id"),
            title = json.optString("title"),
            details = json.optString("details"),
            category = json.optString("category"),
            categoryTe = json.optString("categoryTe"),
            categoryEn = json.optString("categoryEn"),
            place = json.optString("place"),
            date = json.optString("date"),
            evidenceUrl = json.optString("evidenceUrl"),
            mediaUri = json.optString("mediaUri"),
            mediaConsentConfirmed = json.optBoolean("mediaConsentConfirmed", false),
            authorName = json.optString("authorName"),
            role = enumValueOrDefault(
                json.optString("role"),
                ContributorRole.PARTY_MEMBER
            ),
            authorDirectoryId = json.optString("authorDirectoryId"),
            language = enumValueOrDefault(json.optString("language"), AppLanguage.TELUGU),
            status = enumValueOrDefault(
                json.optString("status"),
                LocalDraftStatus.LOCAL_ONLY
            ),
            createdAtEpochMillis = json.optLong("createdAtEpochMillis", now),
            updatedAtEpochMillis = json.optLong("updatedAtEpochMillis", now)
        ).normalizedFromDisk()
    } catch (_: Exception) {
        null
    }

    private fun profileCorrectionToJson(draft: ProfileCorrectionDraft) = JSONObject().apply {
        put("id", draft.id)
        put("directoryId", draft.directoryId)
        put("personName", draft.personName)
        put("role", draft.role)
        put("place", draft.place)
        put("proposedAge", draft.proposedAge ?: JSONObject.NULL)
        put("ageReferenceYear", draft.ageReferenceYear ?: JSONObject.NULL)
        put("evidenceUrl", draft.evidenceUrl)
        put("submitterName", draft.submitterName)
        put("notes", draft.notes)
        put("language", draft.language.name)
        put("status", draft.status.name)
        put("createdAtEpochMillis", draft.createdAtEpochMillis)
        put("updatedAtEpochMillis", draft.updatedAtEpochMillis)
    }

    private fun profileCorrectionFromJson(json: JSONObject): ProfileCorrectionDraft? = try {
        val now = System.currentTimeMillis()
        ProfileCorrectionDraft(
            id = json.optString("id"),
            directoryId = json.optString("directoryId"),
            personName = json.optString("personName"),
            role = json.optString("role"),
            place = json.optString("place"),
            proposedAge = json.optNullableInt("proposedAge"),
            ageReferenceYear = json.optNullableInt("ageReferenceYear"),
            evidenceUrl = json.optString("evidenceUrl"),
            submitterName = json.optString("submitterName"),
            notes = json.optString("notes"),
            language = enumValueOrDefault(json.optString("language"), AppLanguage.TELUGU),
            status = enumValueOrDefault(
                json.optString("status"),
                LocalDraftStatus.PENDING_VERIFICATION
            ),
            createdAtEpochMillis = json.optLong("createdAtEpochMillis", now),
            updatedAtEpochMillis = json.optLong("updatedAtEpochMillis", now)
        ).normalizedFromDisk()
    } catch (_: Exception) {
        null
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        rawValue: String,
        default: T
    ): T = enumValues<T>().firstOrNull { it.name == rawValue } ?: default
}

private object DraftIdGenerator {
    private val allowedIdCharacters = ('A'..'Z').toSet() + ('0'..'9').toSet() + '-'

    @Synchronized
    fun newId(prefix: String): String {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val suffix = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(6)
            .uppercase(Locale.ROOT)
        return "${prefix.uppercase(Locale.ROOT)}-$date-$suffix"
    }

    fun normalizeExisting(value: String): String = value
        .withAsciiNumerals()
        .uppercase(Locale.ROOT)
        .filter(allowedIdCharacters::contains)
        .take(64)

    fun normalizeOrCreate(value: String, prefix: String): String =
        normalizeExisting(value).ifBlank { newId(prefix) }
}

private fun CivicServiceDraft.normalized(now: Long): CivicServiceDraft = copy(
    id = DraftIdGenerator.normalizeOrCreate(id, "PLR"),
    category = category.withAsciiNumerals().trim(),
    categoryTe = categoryTe.withAsciiNumerals().trim(),
    categoryEn = categoryEn.withAsciiNumerals().trim(),
    mandal = mandal.withAsciiNumerals().trim(),
    village = village.withAsciiNumerals().trim(),
    name = name.withAsciiNumerals().trim(),
    phone = phone.withAsciiNumerals().filter { it in '0'..'9' }.take(15),
    details = details.withAsciiNumerals().trim(),
    createdAtEpochMillis = createdAtEpochMillis.takeIf { it > 0L } ?: now,
    updatedAtEpochMillis = now
)

private fun CivicServiceDraft.normalizedFromDisk(): CivicServiceDraft {
    val safeCreatedAt = createdAtEpochMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
    return copy(
        id = DraftIdGenerator.normalizeOrCreate(id, "PLR"),
        category = category.withAsciiNumerals(),
        categoryTe = categoryTe.withAsciiNumerals(),
        categoryEn = categoryEn.withAsciiNumerals(),
        mandal = mandal.withAsciiNumerals(),
        village = village.withAsciiNumerals(),
        name = name.withAsciiNumerals(),
        phone = phone.withAsciiNumerals().filter { it in '0'..'9' }.take(15),
        details = details.withAsciiNumerals(),
        createdAtEpochMillis = safeCreatedAt,
        updatedAtEpochMillis = updatedAtEpochMillis.takeIf { it > 0L } ?: safeCreatedAt
    )
}

private fun ActivityPostDraft.normalized(now: Long): ActivityPostDraft = copy(
    id = DraftIdGenerator.normalizeOrCreate(id, "ACT"),
    title = title.withAsciiNumerals().trim(),
    details = details.withAsciiNumerals().trim(),
    category = category.withAsciiNumerals().trim(),
    categoryTe = categoryTe.withAsciiNumerals().trim(),
    categoryEn = categoryEn.withAsciiNumerals().trim(),
    place = place.withAsciiNumerals().trim(),
    date = date.withAsciiNumerals().trim(),
    evidenceUrl = evidenceUrl.withAsciiNumerals().trim(),
    mediaUri = mediaUri.trim(),
    authorName = authorName.withAsciiNumerals().trim(),
    authorDirectoryId = DraftIdGenerator.normalizeExisting(authorDirectoryId),
    createdAtEpochMillis = createdAtEpochMillis.takeIf { it > 0L } ?: now,
    updatedAtEpochMillis = now
)

private fun ActivityPostDraft.normalizedFromDisk(): ActivityPostDraft {
    val safeCreatedAt = createdAtEpochMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
    return copy(
        id = DraftIdGenerator.normalizeOrCreate(id, "ACT"),
        title = title.withAsciiNumerals(),
        details = details.withAsciiNumerals(),
        category = category.withAsciiNumerals(),
        categoryTe = categoryTe.withAsciiNumerals(),
        categoryEn = categoryEn.withAsciiNumerals(),
        place = place.withAsciiNumerals(),
        date = date.withAsciiNumerals(),
        evidenceUrl = evidenceUrl.withAsciiNumerals(),
        mediaUri = mediaUri.trim(),
        authorName = authorName.withAsciiNumerals(),
        authorDirectoryId = DraftIdGenerator.normalizeExisting(authorDirectoryId),
        createdAtEpochMillis = safeCreatedAt,
        updatedAtEpochMillis = updatedAtEpochMillis.takeIf { it > 0L } ?: safeCreatedAt
    )
}

private fun ProfileCorrectionDraft.normalized(now: Long): ProfileCorrectionDraft = copy(
    id = DraftIdGenerator.normalizeOrCreate(id, "COR"),
    directoryId = DraftIdGenerator.normalizeExisting(directoryId),
    personName = personName.withAsciiNumerals().trim(),
    role = role.withAsciiNumerals().trim(),
    place = place.withAsciiNumerals().trim(),
    proposedAge = proposedAge.validAgeOrNull(),
    ageReferenceYear = ageReferenceYear.validReferenceYearOrNull(),
    evidenceUrl = evidenceUrl.withAsciiNumerals().trim(),
    submitterName = submitterName.withAsciiNumerals().trim(),
    notes = notes.withAsciiNumerals().trim(),
    createdAtEpochMillis = createdAtEpochMillis.takeIf { it > 0L } ?: now,
    updatedAtEpochMillis = now
)

private fun ProfileCorrectionDraft.normalizedFromDisk(): ProfileCorrectionDraft {
    val safeCreatedAt = createdAtEpochMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
    return copy(
        id = DraftIdGenerator.normalizeOrCreate(id, "COR"),
        directoryId = DraftIdGenerator.normalizeExisting(directoryId),
        personName = personName.withAsciiNumerals(),
        role = role.withAsciiNumerals(),
        place = place.withAsciiNumerals(),
        proposedAge = proposedAge.validAgeOrNull(),
        ageReferenceYear = ageReferenceYear.validReferenceYearOrNull(),
        evidenceUrl = evidenceUrl.withAsciiNumerals(),
        submitterName = submitterName.withAsciiNumerals(),
        notes = notes.withAsciiNumerals(),
        createdAtEpochMillis = safeCreatedAt,
        updatedAtEpochMillis = updatedAtEpochMillis.takeIf { it > 0L } ?: safeCreatedAt
    )
}

private fun Int?.validAgeOrNull(): Int? = this?.takeIf { it in 18..120 }

private fun Int?.validReferenceYearOrNull(): Int? {
    val latestAllowedYear = Calendar.getInstance().get(Calendar.YEAR)
    return this?.takeIf { it in 1900..latestAllowedYear }
}

private fun JSONObject.optNullableInt(key: String): Int? =
    if (has(key) && !isNull(key)) optInt(key) else null

/** Converts any Unicode decimal digits to the ASCII digits 0-9. */
private fun String.withAsciiNumerals(): String = buildString(length) {
    this@withAsciiNumerals.forEach { character ->
        val digit = Character.digit(character, 10)
        if (digit in 0..9) append(('0'.code + digit).toChar()) else append(character)
    }
}
