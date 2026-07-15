package com.paleru.congress.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.paleru.congress.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.util.UUID

data class SocialComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
    val viewerCanDelete: Boolean
)

private data class DeviceIdentity(val id: String, val key: String)

/**
 * Small dependency-free client for the private Paleru feed.
 * The MongoDB URI never leaves the server; the app only stores a random device credential.
 */
class PaleruSocialApi(private val context: Context) {
    private val baseUrl = BuildConfig.PALERU_SOCIAL_API_BASE.trimEnd('/')
    private val identity = loadIdentity(context)

    suspend fun listPosts(): Result<List<ActivityPostDraft>> = call {
        val body = request("GET", "/paleru-social/posts?limit=50")
        val posts = body.getJSONArray("posts")
        List(posts.length()) { index -> postFromJson(posts.getJSONObject(index)) }
    }

    suspend fun createPost(draft: ActivityPostDraft): Result<ActivityPostDraft> = call {
        val payload = JSONObject()
            .put("author_name", draft.authorName)
            .put("content", draft.details)
            .put("place", draft.place)
        if (draft.mediaUri.isNotBlank()) {
            payload.put("media_base64", encodeImage(draft.mediaUri))
            payload.put("media_mime", "image/jpeg")
        }
        postFromJson(request("POST", "/paleru-social/posts", payload))
    }

    suspend fun updatePost(draft: ActivityPostDraft): Result<ActivityPostDraft> = call {
        require(draft.remoteId.isNotBlank()) { "This post has not been synced" }
        val payload = JSONObject().put("content", draft.details).put("place", draft.place)
        postFromJson(request("PATCH", "/paleru-social/posts/${draft.remoteId}", payload))
    }

    suspend fun deletePost(remoteId: String): Result<Unit> = call {
        request("DELETE", "/paleru-social/posts/$remoteId")
        Unit
    }

    suspend fun react(remoteId: String, reaction: String): Result<ActivityPostDraft> = call {
        val payload = JSONObject().put("reaction", reaction)
        postFromJson(request("PUT", "/paleru-social/posts/$remoteId/reaction", payload))
    }

    suspend fun listComments(remoteId: String): Result<List<SocialComment>> = call {
        val body = request("GET", "/paleru-social/posts/$remoteId/comments")
        val comments = body.getJSONArray("comments")
        List(comments.length()) { index -> commentFromJson(comments.getJSONObject(index)) }
    }

    suspend fun addComment(remoteId: String, authorName: String, content: String): Result<SocialComment> = call {
        val payload = JSONObject().put("author_name", authorName).put("content", content)
        commentFromJson(request("POST", "/paleru-social/posts/$remoteId/comments", payload))
    }

    suspend fun deleteComment(remoteId: String, commentId: String): Result<Unit> = call {
        request("DELETE", "/paleru-social/posts/$remoteId/comments/$commentId")
        Unit
    }

    private suspend fun <T> call(block: () -> T): Result<T> = withContext(Dispatchers.IO) {
        runCatching(block)
    }

    private fun request(method: String, path: String, payload: JSONObject? = null): JSONObject {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Paleru-Device-Id", identity.id)
            setRequestProperty("X-Paleru-Device-Key", identity.key)
            if (payload != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        return try {
            if (payload != null) {
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val detail = runCatching { JSONObject(text).optString("detail") }.getOrNull().orEmpty()
                throw IllegalStateException(detail.ifBlank { "Server request failed ($status)" })
            }
            if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun postFromJson(json: JSONObject): ActivityPostDraft {
        val content = json.optString("content")
        val createdAt = json.optString("created_at")
        val remoteMedia = json.optString("media_url").takeUnless { it == "null" }.orEmpty()
        return ActivityPostDraft(
            id = "NET-${json.getString("id")}",
            title = content.lineSequence().firstOrNull().orEmpty().take(100),
            details = content,
            category = "party-activity",
            categoryTe = "పార్టీ కార్యకలాపం",
            categoryEn = "Party activity",
            place = json.optString("place"),
            date = createdAt.take(10),
            mediaConsentConfirmed = true,
            authorName = json.optString("author_name", "Congress member"),
            status = LocalDraftStatus.PENDING_VERIFICATION,
            remoteId = json.getString("id"),
            remoteMediaUrl = if (remoteMedia.startsWith("/")) baseUrl + remoteMedia else remoteMedia,
            likeCount = json.optInt("like_count"),
            dislikeCount = json.optInt("dislike_count"),
            commentCount = json.optInt("comment_count"),
            viewerReaction = json.optString("viewer_reaction", "none"),
            viewerCanEdit = json.optBoolean("viewer_can_edit")
        )
    }

    private fun commentFromJson(json: JSONObject) = SocialComment(
        id = json.getString("id"),
        postId = json.getString("post_id"),
        authorName = json.optString("author_name", "Congress member"),
        content = json.optString("content"),
        createdAt = json.optString("created_at").take(10),
        viewerCanDelete = json.optBoolean("viewer_can_delete")
    )

    private fun encodeImage(value: String): String {
        val uri = Uri.parse(value)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: error("The selected photo cannot be opened")
        var sample = 1
        while (bounds.outWidth / sample > 2000 || bounds.outHeight / sample > 2000) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("The selected photo cannot be decoded")
        val ratio = minOf(1f, 1600f / maxOf(decoded.width, decoded.height).toFloat())
        val output: Bitmap = if (ratio < 1f) {
            Bitmap.createScaledBitmap(decoded, (decoded.width * ratio).toInt(), (decoded.height * ratio).toInt(), true)
        } else decoded
        val bytes = ByteArrayOutputStream().use { stream ->
            check(output.compress(Bitmap.CompressFormat.JPEG, 82, stream)) { "The selected photo cannot be compressed" }
            stream.toByteArray()
        }
        if (output !== decoded) output.recycle()
        decoded.recycle()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun loadIdentity(context: Context): DeviceIdentity {
        val preferences = context.getSharedPreferences("paleru_social_identity_v1", Context.MODE_PRIVATE)
        val savedId = preferences.getString("device_id", null)
        val savedKey = preferences.getString("device_key", null)
        if (!savedId.isNullOrBlank() && !savedKey.isNullOrBlank()) return DeviceIdentity(savedId, savedKey)
        val keyBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val identity = DeviceIdentity(
            id = "android-${UUID.randomUUID()}",
            key = Base64.encodeToString(keyBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        )
        preferences.edit().putString("device_id", identity.id).putString("device_key", identity.key).commit()
        return identity
    }
}
