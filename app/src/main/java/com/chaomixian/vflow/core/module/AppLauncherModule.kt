package com.chaomixian.vflow.core.module

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.workflow_editor.PillUtil

class AppLauncherModule : BaseModule() {

    override val id = "vflow.automation.launch_app"
    override val metadata = ActionMetadata(
        name = "启动应用",
        description = "通过包名启动指定的应用程序。",
        iconRes = R.drawable.rounded_play_arrow_24,
        category = "应用与系统",
        categoryId = "device"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.LOW,
        directToolDescription = "Launch an app by its package name.",
        workflowStepDescription = "Launch an app by package name.",
        inputHints = mapOf(
            "packageName" to "Android package name of the target app."
        ),
        requiredInputIds = setOf("packageName")
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "packageName",
            name = "应用包名",
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
        val pkgPill = PillUtil.createPillFromParam(
            step.parameters["packageName"],
            inputs.find { it.id == "packageName" }
        )
        return PillUtil.buildSpannable(context, "启动 ", pkgPill)
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val packageName = context.getVariableAsString("packageName", "")

        if (packageName.isBlank()) {
            return ExecutionResult.Failure("参数错误", "应用包名不能为空")
        }

        onProgress(ProgressUpdate("正在启动应用: $packageName"))

        val intent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            return ExecutionResult.Failure("启动失败", "找不到应用: $packageName")
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            appContext.startActivity(intent)
            onProgress(ProgressUpdate("应用已启动: $packageName"))
            ExecutionResult.Success(mapOf("success" to VBoolean(true)))
        } catch (e: Exception) {
            ExecutionResult.Failure("启动失败", e.localizedMessage ?: "未知错误")
        }
    }
}
