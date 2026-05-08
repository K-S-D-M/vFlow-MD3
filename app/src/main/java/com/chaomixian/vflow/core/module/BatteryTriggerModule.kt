package com.chaomixian.vflow.core.module

import android.content.Context
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.workflow.model.ActionStep

class BatteryTriggerModule : BaseModule() {
    override val id = "vflow.trigger.battery"
    override val metadata = ActionMetadata(
        name = "电量触发",
        description = "根据电量状态触发工作流",
        iconRes = R.drawable.rounded_battery_android_frame_full_24,
        category = "触发器",
        categoryId = ModuleCategories.TRIGGER
    )

    companion object {
        const val TYPE_CHARGING = "charging"
        const val TYPE_DISCHARGING = "discharging"
        const val TYPE_LOW_BATTERY = "low_battery"
        const val TYPE_FULL = "full"
    }

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "triggerType",
            name = "触发类型",
            staticType = ParameterType.ENUM,
            defaultValue = TYPE_CHARGING,
            options = listOf(TYPE_CHARGING, TYPE_DISCHARGING, TYPE_LOW_BATTERY, TYPE_FULL),
            inputStyle = InputStyle.CHIP_GROUP,
            acceptsMagicVariable = false,
            acceptsNamedVariable = false
        ),
        InputDefinition(
            id = "thresholdLevel",
            name = "电量阈值",
            staticType = ParameterType.NUMBER,
            defaultValue = 50,
            inputStyle = InputStyle.SLIDER,
            sliderConfig = InputDefinition.slider(0f, 100f, 1f),
            acceptsMagicVariable = false,
            acceptsNamedVariable = false
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = emptyList()

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val triggerType = step.parameters["triggerType"] as? String ?: TYPE_CHARGING
        val thresholdLevel = (step.parameters["thresholdLevel"] as? Number)?.toInt() ?: 50

        val typeLabel = when (triggerType) {
            TYPE_CHARGING -> "充电中"
            TYPE_DISCHARGING -> "放电中"
            TYPE_LOW_BATTERY -> "低电量"
            TYPE_FULL -> "已充满"
            else -> triggerType
        }

        return "$typeLabel 且电量 $thresholdLevel% 时触发"
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        onProgress(ProgressUpdate("电量触发已激活"))
        return ExecutionResult.Success()
    }
}
