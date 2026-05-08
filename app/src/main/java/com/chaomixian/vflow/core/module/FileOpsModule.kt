package com.chaomixian.vflow.core.module

import android.content.Context
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.types.basic.VString
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.workflow_editor.PillUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileOpsModule : BaseModule() {

    companion object {
        private const val OP_COPY = "copy"
        private const val OP_MOVE = "move"
        private const val OP_DELETE = "delete"
        private const val OP_RENAME = "rename"
    }

    override val id = "vflow.automation.file_operations"
    override val metadata = ActionMetadata(
        name = "文件操作",
        description = "对文件进行复制、移动、删除、重命名操作。",
        iconRes = R.drawable.rounded_content_copy_24,
        category = "文件",
        categoryId = "file"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.STANDARD,
        directToolDescription = "Perform file operations: copy, move, delete, or rename.",
        workflowStepDescription = "Perform a file operation.",
        inputHints = mapOf(
            "operation" to "File operation type: copy, move, delete, or rename.",
            "sourcePath" to "Absolute path to the source file or directory.",
            "destinationPath" to "Absolute path to the destination (for copy, move, rename)."
        ),
        requiredInputIds = setOf("operation", "sourcePath")
    )

    private val operationOptions = listOf(OP_COPY, OP_MOVE, OP_DELETE, OP_RENAME)

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "operation",
            name = "操作类型",
            staticType = ParameterType.ENUM,
            defaultValue = OP_COPY,
            options = operationOptions,
            acceptsMagicVariable = false
        ),
        InputDefinition(
            id = "sourcePath",
            name = "源文件路径",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        ),
        InputDefinition(
            id = "destinationPath",
            name = "目标路径",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true,
            visibility = InputVisibility.notEquals("operation", OP_DELETE)
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("success", "是否成功", VTypeRegistry.BOOLEAN.id),
        OutputDefinition("resultPath", "结果路径", VTypeRegistry.STRING.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val opPill = PillUtil.createPillFromParam(
            step.parameters["operation"],
            inputs.find { it.id == "operation" },
            isModuleOption = true
        )
        val sourcePill = PillUtil.createPillFromParam(
            step.parameters["sourcePath"],
            inputs.find { it.id == "sourcePath" }
        )
        return PillUtil.buildSpannable(context, opPill, " ", sourcePill)
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val rawOperation = context.getVariableAsString("operation", OP_COPY)
        val operation = getInputs().find { it.id == "operation" }?.normalizeEnumValue(rawOperation) ?: rawOperation
        val sourcePath = context.getVariableAsString("sourcePath", "")
        val destinationPath = context.getVariableAsString("destinationPath", "")

        if (sourcePath.isEmpty()) {
            return ExecutionResult.Failure("参数错误", "源文件路径不能为空")
        }

        onProgress(ProgressUpdate("正在执行文件操作: $operation"))

        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(sourcePath)
                when (operation) {
                    OP_COPY -> {
                        if (!sourceFile.exists()) {
                            return@withContext ExecutionResult.Failure("文件不存在", "源文件不存在: $sourcePath")
                        }
                        if (destinationPath.isEmpty()) {
                            return@withContext ExecutionResult.Failure("参数错误", "复制操作需要指定目标路径")
                        }
                        val destFile = File(destinationPath)
                        sourceFile.copyRecursively(destFile, overwrite = true)
                        onProgress(ProgressUpdate("复制成功"))
                        ExecutionResult.Success(mapOf(
                            "success" to VBoolean(true),
                            "resultPath" to VString(destFile.absolutePath)
                        ))
                    }
                    OP_MOVE -> {
                        if (!sourceFile.exists()) {
                            return@withContext ExecutionResult.Failure("文件不存在", "源文件不存在: $sourcePath")
                        }
                        if (destinationPath.isEmpty()) {
                            return@withContext ExecutionResult.Failure("参数错误", "移动操作需要指定目标路径")
                        }
                        val destFile = File(destinationPath)
                        val moved = sourceFile.renameTo(destFile)
                        if (moved) {
                            onProgress(ProgressUpdate("移动成功"))
                            ExecutionResult.Success(mapOf(
                                "success" to VBoolean(true),
                                "resultPath" to VString(destFile.absolutePath)
                            ))
                        } else {
                            ExecutionResult.Failure("移动失败", "无法将文件从 $sourcePath 移动到 $destinationPath")
                        }
                    }
                    OP_DELETE -> {
                        if (!sourceFile.exists()) {
                            return@withContext ExecutionResult.Failure("文件不存在", "源文件不存在: $sourcePath")
                        }
                        val deleted = if (sourceFile.isDirectory) {
                            sourceFile.deleteRecursively()
                        } else {
                            sourceFile.delete()
                        }
                        if (deleted) {
                            onProgress(ProgressUpdate("删除成功"))
                            ExecutionResult.Success(mapOf(
                                "success" to VBoolean(true),
                                "resultPath" to VString("")
                            ))
                        } else {
                            ExecutionResult.Failure("删除失败", "无法删除: $sourcePath")
                        }
                    }
                    OP_RENAME -> {
                        if (!sourceFile.exists()) {
                            return@withContext ExecutionResult.Failure("文件不存在", "源文件不存在: $sourcePath")
                        }
                        if (destinationPath.isEmpty()) {
                            return@withContext ExecutionResult.Failure("参数错误", "重命名操作需要指定新名称")
                        }
                        val destFile = File(sourceFile.parent, destinationPath)
                        val renamed = sourceFile.renameTo(destFile)
                        if (renamed) {
                            onProgress(ProgressUpdate("重命名成功"))
                            ExecutionResult.Success(mapOf(
                                "success" to VBoolean(true),
                                "resultPath" to VString(destFile.absolutePath)
                            ))
                        } else {
                            ExecutionResult.Failure("重命名失败", "无法将 $sourcePath 重命名为 $destinationPath")
                        }
                    }
                    else -> ExecutionResult.Failure("操作失败", "未知的操作类型: $operation")
                }
            } catch (e: Exception) {
                ExecutionResult.Failure("文件操作失败", e.message ?: "未知错误")
            }
        }
    }
}
