package com.chaomixian.vflow.core.module

import android.content.Context
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.workflow.model.ActionStep

class TimeTriggerModule : BaseModule() {
    override val id = "vflow.trigger.time"
    override val metadata = ActionMetadata(
        name = "定时触发",
        description = "在指定时间执行工作流",
        iconRes = R.drawable.rounded_repeat_24,
        category = "触发器",
        categoryId = ModuleCategories.TRIGGER
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "time",
            name = "触发时间",
            staticType = ParameterType.STRING,
            defaultValue = "09:00",
            acceptsMagicVariable = false,
            hint = "HH:mm"
        ),
        InputDefinition(
            id = "repeatDaily",
            name = "每日重复",
            staticType = ParameterType.BOOLEAN,
            defaultValue = true,
            acceptsMagicVariable = false,
            acceptsNamedVariable = false
        ),
        InputDefinition(
            id = "daysOfWeek",
            name = "重复日期",
            staticType = ParameterType.STRING,
            defaultValue = "1,2,3,4,5",
            acceptsMagicVariable = false,
            acceptsNamedVariable = false,
            hint = "1=周一,7=周日",
            visibility = InputVisibility.whenTrue("repeatDaily")
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = emptyList()

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val time = step.parameters["time"] as? String ?: "09:00"
        val repeatDaily = step.parameters["repeatDaily"] as? Boolean ?: true
        val daysOfWeek = step.parameters["daysOfWeek"] as? String ?: "1,2,3,4,5"

        return if (repeatDaily) {
            "每周 $daysOfWeek $time 触发"
        } else {
            "$time 触发一次"
        }
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        onProgress(ProgressUpdate("定时任务已触发"))
        return ExecutionResult.Success()
    }
}
