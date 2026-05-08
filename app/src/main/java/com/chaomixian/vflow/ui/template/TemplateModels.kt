package com.chaomixian.vflow.ui.template

import androidx.annotation.DrawableRes
import com.chaomixian.vflow.R

enum class TemplateCategory(val label: String) {
    ALL("全部"),
    DAILY("日常"),
    CHECKIN("签到"),
    FILE("文件管理"),
    SYSTEM("系统维护")
}

data class WorkflowTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    @DrawableRes val iconRes: Int,
    val downloadCount: Int,
    val previewDescription: String
)

object TemplateRepository {

    val templates: List<WorkflowTemplate> = listOf(
        WorkflowTemplate(
            id = "daily_checkin",
            name = "每日签到",
            description = "自动完成各应用每日签到",
            category = TemplateCategory.CHECKIN,
            iconRes = R.drawable.rounded_star_shine_24,
            downloadCount = 12580,
            previewDescription = "支持多平台自动签到，包括支付宝、京东等常见应用的每日签到任务"
        ),
        WorkflowTemplate(
            id = "auto_backup",
            name = "自动备份",
            description = "定期备份指定文件到目标位置",
            category = TemplateCategory.FILE,
            iconRes = R.drawable.rounded_save_24,
            downloadCount = 8430,
            previewDescription = "按设定的时间间隔自动将指定文件夹内容备份到外部存储或云端"
        ),
        WorkflowTemplate(
            id = "screen_tap",
            name = "屏幕点击",
            description = "自动化屏幕点击操作",
            category = TemplateCategory.DAILY,
            iconRes = R.drawable.rounded_ads_click_24,
            downloadCount = 15720,
            previewDescription = "录制并回放屏幕点击序列，支持循环执行和条件触发"
        ),
        WorkflowTemplate(
            id = "battery_monitor",
            name = "电量监控",
            description = "监控电量变化并发出提醒",
            category = TemplateCategory.SYSTEM,
            iconRes = R.drawable.rounded_battery_android_frame_full_24,
            downloadCount = 6210,
            previewDescription = "实时监控设备电量，在电量低于阈值或充满时发送通知提醒"
        ),
        WorkflowTemplate(
            id = "file_organizer",
            name = "文件整理",
            description = "自动整理下载文件夹",
            category = TemplateCategory.FILE,
            iconRes = R.drawable.ic_folder,
            downloadCount = 9870,
            previewDescription = "按文件类型自动分类整理下载目录，保持文件夹整洁有序"
        ),
        WorkflowTemplate(
            id = "app_launcher",
            name = "应用启动器",
            description = "按计划定时启动应用",
            category = TemplateCategory.DAILY,
            iconRes = R.drawable.rounded_play_arrow_24,
            downloadCount = 7350,
            previewDescription = "根据设定的时间表自动启动指定应用，支持工作日和周末不同计划"
        ),
        WorkflowTemplate(
            id = "wifi_switch",
            name = "Wi-Fi切换",
            description = "根据位置自动切换Wi-Fi",
            category = TemplateCategory.SYSTEM,
            iconRes = R.drawable.rounded_wifi_tethering_24,
            downloadCount = 5680,
            previewDescription = "基于地理位置自动连接或断开指定Wi-Fi网络，节省电量"
        ),
        WorkflowTemplate(
            id = "notification_forward",
            name = "通知转发",
            description = "将通知转发到其他应用",
            category = TemplateCategory.DAILY,
            iconRes = R.drawable.rounded_notifications_unread_24,
            downloadCount = 11240,
            previewDescription = "监听指定应用的通知并自动转发到钉钉、飞书等即时通讯工具"
        )
    )

    fun getByCategory(category: TemplateCategory): List<WorkflowTemplate> {
        if (category == TemplateCategory.ALL) return templates
        return templates.filter { it.category == category }
    }

    fun search(query: String): List<WorkflowTemplate> {
        if (query.isBlank()) return templates
        val normalized = query.trim().lowercase()
        return templates.filter {
            it.name.lowercase().contains(normalized) ||
                    it.description.lowercase().contains(normalized) ||
                    it.previewDescription.lowercase().contains(normalized)
        }
    }
}
