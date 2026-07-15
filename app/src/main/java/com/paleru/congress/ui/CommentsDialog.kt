package com.paleru.congress.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paleru.congress.data.AppLanguage
import com.paleru.congress.data.SocialComment

@Composable
internal fun CommentsDialog(
    language: AppLanguage,
    comments: List<SocialComment>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onDelete: (SocialComment) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(language, "వ్యాఖ్యలు", "Comments"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    loading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                    comments.isEmpty() -> Text(
                        tr(language, "మొదటి వ్యాఖ్యను రాయండి.", "Start the conversation."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 330.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comments, key = { it.id }) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InitialsAvatar(
                                    name = comment.authorName,
                                    contentDescription = tr(language, "వ్యాఖ్య రచయిత", "Comment author"),
                                    size = 36
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comment.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                                    Text(comment.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (comment.viewerCanDelete) {
                                    IconButton(onClick = { onDelete(comment) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = tr(language, "వ్యాఖ్య తొలగించండి", "Delete comment"))
                                    }
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(1000) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(tr(language, "వ్యాఖ్య రాయండి…", "Write a comment…")) },
                    trailingIcon = {
                        IconButton(
                            enabled = text.trim().isNotEmpty() && !loading,
                            onClick = {
                                onSend(text.trim())
                                text = ""
                            }
                        ) { Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = tr(language, "పంపండి", "Send")) }
                    },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr(language, "మూసివేయండి", "Close")) } }
    )
}
