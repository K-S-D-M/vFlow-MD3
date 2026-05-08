package com.chaomixian.vflow.ui.logs

import android.text.format.DateUtils
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.logging.LogEntry
import com.chaomixian.vflow.core.logging.LogManager
import com.chaomixian.vflow.core.logging.LogStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LogFilter(val label: String) {
    ALL("全部"),
    SUCCESS("成功"),
    FAILED("失败")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionLogScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var allLogs by remember { mutableStateOf(LogManager.getAllLogs()) }
    var selectedFilter by remember { mutableStateOf(LogFilter.ALL) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }

    val filteredLogs = remember(allLogs, selectedFilter) {
        when (selectedFilter) {
            LogFilter.ALL -> allLogs
            LogFilter.SUCCESS -> allLogs.filter { it.status == LogStatus.SUCCESS }
            LogFilter.FAILED -> allLogs.filter { it.status != LogStatus.SUCCESS }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = {
                        selectedFilter = filter
                        allLogs = LogManager.getAllLogs()
                    },
                    label = { Text(filter.label) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = { showClearConfirmDialog = true },
                enabled = allLogs.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("清除全部")
            }
        }

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无执行日志",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = filteredLogs,
                    key = { log -> "${log.workflowId}-${log.timestamp}" }
                ) { log ->
                    ExecutionLogCard(
                        log = log,
                        onClick = { selectedLog = log }
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("清除全部日志") },
            text = { Text("确定要清除所有执行日志吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        LogManager.clearLogs()
                        allLogs = emptyList()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    selectedLog?.let { log ->
        LogDetailDialog(
            log = log,
            onDismiss = { selectedLog = null },
            onDelete = {
                LogManager.deleteLog(it)
                allLogs = LogManager.getAllLogs()
                selectedLog = null
            }
        )
    }
}

@Composable
private fun ExecutionLogCard(
    log: LogEntry,
    onClick: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    val iconRes = when (log.status) {
        LogStatus.SUCCESS -> R.drawable.ic_log_success
        LogStatus.FAILURE, LogStatus.CANCELLED -> R.drawable.ic_log_failure
    }
    val iconTint = when (log.status) {
        LogStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        LogStatus.FAILURE, LogStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    val statusLabel = when (log.status) {
        LogStatus.SUCCESS -> "成功"
        LogStatus.FAILURE -> "失败"
        LogStatus.CANCELLED -> "已取消"
    }
    val statusColor = when (log.status) {
        LogStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        LogStatus.FAILURE -> MaterialTheme.colorScheme.error
        LogStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.workflowName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                log.resolveMessage(LocalContext.current)?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun LogDetailDialog(
    log: LogEntry,
    onDismiss: () -> Unit,
    onDelete: (LogEntry) -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDelete(log) }) {
                Text("删除")
            }
        },
        title = {
            Text(
                text = log.workflowName,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "执行时间: ${dateFormat.format(Date(log.timestamp))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = log.resolveMessage(context) ?: "无详细信息",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!log.detailedLog.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "详细日志",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = log.detailedLog,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}
