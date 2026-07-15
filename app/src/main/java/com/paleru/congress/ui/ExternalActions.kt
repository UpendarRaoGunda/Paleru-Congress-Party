package com.paleru.congress.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

internal fun openMap(context: Context, query: String): Boolean {
    val encoded = Uri.encode(query)
    val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(geoIntent)
        true
    } catch (_: ActivityNotFoundException) {
        openUrl(
            context,
            "https://www.google.com/maps/search/?api=1&query=$encoded"
        )
    }
}

internal fun openUrl(context: Context, url: String): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (_: ActivityNotFoundException) {
    false
}

internal fun dialNumber(context: Context, number: String): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (_: ActivityNotFoundException) {
    false
}

internal fun shareText(context: Context, subject: String, body: String): Boolean = try {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(
        Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (_: ActivityNotFoundException) {
    false
}

internal fun shareMediaText(
    context: Context,
    mediaUri: Uri,
    subject: String,
    body: String
): Boolean = try {
    val mediaType = context.contentResolver.getType(mediaUri) ?: "image/*"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mediaType
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        putExtra(Intent.EXTRA_STREAM, mediaUri)
        clipData = ClipData.newUri(context.contentResolver, subject, mediaUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}

/** Keeps a picked image readable after process death, copying into private storage when needed. */
internal fun retainPickedActivityImage(context: Context, source: Uri): Uri? {
    try {
        context.contentResolver.takePersistableUriPermission(
            source,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return source
    } catch (_: SecurityException) {
        // Providers without persistable grants are copied below.
    } catch (_: IllegalArgumentException) {
        // The URI does not support persistable permission; use the private fallback.
    }

    val mimeType = context.contentResolver.getType(source)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        ?.takeIf { it.matches(Regex("[a-zA-Z0-9]{1,8}")) }
        ?: "jpg"
    val directory = File(context.filesDir, ACTIVITY_MEDIA_DIRECTORY)
    if (!directory.exists() && !directory.mkdirs()) return null
    val destination = File(directory, "activity-${UUID.randomUUID()}.$extension")

    return try {
        val input = context.contentResolver.openInputStream(source) ?: return null
        input.use { sourceStream ->
            destination.outputStream().use { outputStream -> sourceStream.copyTo(outputStream) }
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            destination
        )
    } catch (_: Exception) {
        destination.delete()
        null
    }
}

internal fun deleteOwnedActivityImage(context: Context, mediaUri: String): Boolean {
    val uri = runCatching { Uri.parse(mediaUri) }.getOrNull() ?: return false
    if (uri.authority != "${context.packageName}.fileprovider") {
        return try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            true
        } catch (_: SecurityException) {
            false
        }
    }
    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: return false
    if (!fileName.startsWith("activity-") || fileName.contains("..")) return false
    val ownedFile = File(File(context.filesDir, ACTIVITY_MEDIA_DIRECTORY), fileName)
    return !ownedFile.exists() || ownedFile.delete()
}

internal fun deleteAllOwnedActivityImages(context: Context): Boolean {
    val directory = File(context.filesDir, ACTIVITY_MEDIA_DIRECTORY)
    return !directory.exists() || directory.deleteRecursively()
}

private const val ACTIVITY_MEDIA_DIRECTORY = "activity_media"
