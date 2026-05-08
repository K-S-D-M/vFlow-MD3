package com.chaomixian.vflow.core.module

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.types.basic.VNumber
import com.chaomixian.vflow.core.types.complex.VCoordinate
import com.chaomixian.vflow.core.types.complex.VScreenElement
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.services.ServiceStateBus
import com.chaomixian.vflow.ui.workflow_editor.PillUtil
import kotlinx.coroutines.delay

class WaitForElementModule : BaseModule() {

    companion object {
        private const val MATCH_CONTAINS = "contains"
        private const val MATCH_EXACT = "exact"
        private const val MATCH_REGEX = "regex"
    }

    override val id = "vflow.automation.wait_for_element"
    override val metadata = ActionMetadata(
        name = "等待元素出现",
        description = "等待屏幕上出现指定的文本或元素，支持超时设置。",
        iconRes = R.drawable.rounded_search_24,
        category = "界面交互",
        categoryId = "interaction"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.READ_ONLY,
        directToolDescription = "Wait for a specific text or element to appear on screen.",
        workflowStepDescription = "Wait for a text or element to appear on screen.",
        inputHints = mapOf(
            "targetText" to "The text to wait for on screen.",
            "timeout" to "Maximum wait time in seconds."
        ),
        requiredInputIds = setOf("targetText")
    )

    private val matchModeOptions = listOf(MATCH_CONTAINS, MATCH_EXACT, MATCH_REGEX)

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "targetText",
            name = "目标文本",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        ),
        InputDefinition(
            id = "matchMode",
            name = "匹配模式",
            staticType = ParameterType.ENUM,
            defaultValue = MATCH_CONTAINS,
            options = matchModeOptions,
            acceptsMagicVariable = false
        ),
        InputDefinition(
            id = "timeout",
            name = "超时时间(秒)",
            staticType = ParameterType.NUMBER,
            defaultValue = 10.0,
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.NUMBER.id)
        ),
        InputDefinition(
            id = "interval",
            name = "轮询间隔(ms)",
            staticType = ParameterType.NUMBER,
            defaultValue = 1000.0,
            acceptsMagicVariable = true,
            isFolded = true
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("found", "是否找到", VTypeRegistry.BOOLEAN.id),
        OutputDefinition("element", "找到的元素", VTypeRegistry.SCREEN_ELEMENT.id),
        OutputDefinition("coordinate", "中心坐标", VTypeRegistry.COORDINATE.id),
        OutputDefinition("elapsedTime", "耗时(ms)", VTypeRegistry.NUMBER.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val targetPill = PillUtil.createPillFromParam(
            step.parameters["targetText"],
            inputs.find { it.id == "targetText" }
        )
        val timeoutPill = PillUtil.createPillFromParam(
            step.parameters["timeout"],
            inputs.find { it.id == "timeout" }
        )
        return PillUtil.buildSpannable(
            context,
            "等待 ",
            targetPill,
            " 超时 ",
            timeoutPill,
            " 秒"
        )
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val targetText = context.getVariableAsString("targetText", "")
        val rawMatchMode = context.getVariableAsString("matchMode", MATCH_CONTAINS)
        val matchMode = getInputs().find { it.id == "matchMode" }?.normalizeEnumValue(rawMatchMode) ?: rawMatchMode
        val timeoutSec = context.getVariableAsLong("timeout") ?: 10
        val interval = (context.getVariableAsLong("interval") ?: 1000).coerceAtLeast(100)

        if (targetText.isEmpty()) {
            return ExecutionResult.Failure("参数错误", "目标文本不能为空")
        }

        val service = ServiceStateBus.getAccessibilityService()
        if (service == null) {
            return ExecutionResult.Failure("服务未连接", "无障碍服务未启动，无法查找元素")
        }

        onProgress(ProgressUpdate("正在等待元素出现: $targetText"))
        val startTime = System.currentTimeMillis()
        val timeoutMs = timeoutSec * 1000

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val root = service.rootInActiveWindow
            if (root != null) {
                val node = findNode(root, targetText, matchMode)
                if (node != null) {
                    val element = VScreenElement.fromAccessibilityNode(node)
                    val coordinate = VCoordinate(element.bounds.centerX(), element.bounds.centerY())
                    val elapsed = System.currentTimeMillis() - startTime
                    node.recycle()
                    onProgress(ProgressUpdate("已找到元素，耗时 ${elapsed}ms"))
                    return ExecutionResult.Success(mapOf(
                        "found" to VBoolean(true),
                        "element" to element,
                        "coordinate" to coordinate,
                        "elapsedTime" to VNumber(elapsed.toDouble())
                    ))
                }
            }
            delay(interval)
        }

        onProgress(ProgressUpdate("等待超时，未找到元素"))
        return ExecutionResult.Success(mapOf(
            "found" to VBoolean(false),
            "elapsedTime" to VNumber(timeoutMs.toDouble())
        ))
    }

    private fun findNode(root: AccessibilityNodeInfo, text: String, mode: String): AccessibilityNodeInfo? {
        val queue = java.util.ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (!queue.isEmpty()) {
            val node = queue.removeFirst()
            val nodeText = node.text?.toString() ?: node.contentDescription?.toString()
            var isMatch = false
            if (!nodeText.isNullOrEmpty()) {
                isMatch = when (mode) {
                    MATCH_EXACT -> nodeText == text
                    MATCH_REGEX -> try { Regex(text).containsMatchIn(nodeText) } catch (e: Exception) { false }
                    else -> nodeText.contains(text, ignoreCase = true)
                }
            }
            if (isMatch && node.isVisibleToUser) {
                return AccessibilityNodeInfo.obtain(node)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }
}
