package com.nammasanthe.ledger.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.nammasanthe.ledger.ui.theme.PaidGreen
import com.nammasanthe.ledger.ui.theme.PendingRed
import com.nammasanthe.ledger.util.DueDateFormatter

@Composable
fun DueDateLabel(dueDate: Long?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val status = DueDateFormatter.status(context, dueDate)
    val color = when {
        !status.hasDue -> MaterialTheme.colorScheme.onSurfaceVariant
        status.isOverdue -> PendingRed
        else -> PaidGreen
    }
    val dateStr = DueDateFormatter.formatDueDate(dueDate)
    val text = if (dateStr != null) "$dateStr · ${status.label}" else status.label
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
