package com.chaomixian.vflow.ui.workflow_editor

import com.chaomixian.vflow.core.workflow.model.ActionStep
import java.util.UUID

object StepClipboard {
    private var copiedSteps: List<ActionStep> = emptyList()
    private var sourceWorkflowId: String? = null

    fun copy(steps: List<ActionStep>, workflowId: String?) {
        copiedSteps = steps.map { step ->
            step.copy(
                parameters = deepCopyParameters(step.parameters),
                id = step.id
            )
        }
        sourceWorkflowId = workflowId
    }

    fun hasContent(): Boolean = copiedSteps.isNotEmpty()

    fun getPasteCount(): Int = copiedSteps.size

    fun paste(): List<ActionStep> {
        if (copiedSteps.isEmpty()) return emptyList()
        return copiedSteps.map { step ->
            step.copy(
                parameters = deepCopyParameters(step.parameters),
                id = UUID.randomUUID().toString()
            )
        }
    }

    fun clear() {
        copiedSteps = emptyList()
        sourceWorkflowId = null
    }

    private fun deepCopyParameters(parameters: Map<String, Any?>): Map<String, Any?> {
        return parameters.mapValues { (_, value) -> deepCopyValue(value) }
    }

    private fun deepCopyValue(value: Any?): Any? {
        return when (value) {
            is Map<*, *> -> value.entries.associate { (key, mapValue) ->
                key.toString() to deepCopyValue(mapValue)
            }
            is List<*> -> value.map { item -> deepCopyValue(item) }
            else -> value
        }
    }
}
