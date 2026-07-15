package com.paleru.congress.data

import android.content.Context

/**
 * Device-local feed interactions. These values are never represented as team-wide counts.
 */
object FeedInteractionStore {
    private const val preferencesName = "paleru_feed_interactions_v1"
    private const val likedKey = "liked_post_ids"
    private const val bookmarkedKey = "bookmarked_post_ids"

    fun isLiked(context: Context, postId: String): Boolean =
        readSet(context, likedKey).contains(postId)

    fun isBookmarked(context: Context, postId: String): Boolean =
        readSet(context, bookmarkedKey).contains(postId)

    @Synchronized
    fun toggleLiked(context: Context, postId: String): Boolean =
        toggle(context, likedKey, postId)

    @Synchronized
    fun toggleBookmarked(context: Context, postId: String): Boolean =
        toggle(context, bookmarkedKey, postId)

    @Synchronized
    fun clearAll(context: Context): Boolean = preferences(context).edit().clear().commit()

    @Synchronized
    fun removePost(context: Context, postId: String): Boolean {
        val likes = readSet(context, likedKey).toMutableSet().apply { remove(postId) }
        val bookmarks = readSet(context, bookmarkedKey).toMutableSet().apply { remove(postId) }
        return preferences(context).edit()
            .putStringSet(likedKey, likes)
            .putStringSet(bookmarkedKey, bookmarks)
            .commit()
    }

    private fun toggle(context: Context, key: String, postId: String): Boolean {
        val values = readSet(context, key).toMutableSet()
        val enabled = if (postId in values) {
            values.remove(postId)
            false
        } else {
            values.add(postId)
            true
        }
        preferences(context).edit().putStringSet(key, values).apply()
        return enabled
    }

    private fun readSet(context: Context, key: String): Set<String> =
        preferences(context).getStringSet(key, emptySet())?.toSet().orEmpty()

    private fun preferences(context: Context) = context.applicationContext.getSharedPreferences(
        preferencesName,
        Context.MODE_PRIVATE
    )
}
