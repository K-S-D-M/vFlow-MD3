package com.chaomixian.vflow.core.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.workflow_editor.PillUtil

class PushNotificationModule : BaseModule() {

    companion object {
        private const val CHANNEL_ID = "vflow_automation_notifications"
    }

    override val id = "vflow.automation.send_notification"
    override val metadata = ActionMetadata(
        name = "发送通知",
        description = "发送自定义通知到系统通知栏。",
        iconRes = R.drawable.rounded_notifications_unread_24,
        category = "应用与系统",
        categoryId = "device"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.STANDARD,
        directToolDescription = "Send a custom notification to the system notification bar.",
        workflowStepDescription = "Send a custom notification.",
        inputHints = mapOf(
            "title" to "Notification title.",
            "message" to "Notification body text."
        ),
        requiredInputIds = setOf("title", "message")
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "title",
            name = "标题",
            staticType = ParameterType.STRING,
            defaultValue = "vFlow 通知",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        ),
        InputDefinition(
            id = "message",
            name = "内容",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("success", "是否成功", VTypeRegistry.BOOLEAN.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val titlePill = PillUtil.createPillFromParam(
            step.parameters["title"],
            inputs.find { it.id == "title" }
        )
        val messagePill = PillUtil.createPillFromParam(
            step.parameters["message"],
            inputs.find { it.id == "message" }
        )
        return PillUtil.buildSpannable(
            context,
            "通知 ",
            titlePill,
            ": ",
            messagePill
        )
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val title = context.getVariableAsString("title", "vFlow 通知")
        val message = context.getVariableAsString("message", "")

        val appContext = context.applicationContext
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "vFlow 自动化通知",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_workflows)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        onProgress(ProgressUpdate("通知已发送: $title"))
        return ExecutionResult.Success(mapOf("success" to VBoolean(true)))
    }
}
