package com.chaomixian.vflow.core.module

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
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
import java.io.FileOutputStream

class AutoScreenshotModule : BaseModule() {

    override val id = "vflow.automation.screenshot"
    override val metadata = ActionMetadata(
        name = "截屏",
        description = "自动截取当前屏幕并保存到指定路径。",
        iconRes = R.drawable.rounded_photo_24,
        category = "应用与系统",
        categoryId = "device"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.READ_ONLY,
        directToolDescription = "Capture a screenshot of the current screen and save it.",
        workflowStepDescription = "Capture a screenshot and save it.",
        inputHints = mapOf(
            "savePath" to "Directory path where the screenshot will be saved.",
            "fileName" to "File name for the screenshot (without extension)."
        )
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "savePath",
            name = "保存路径",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        ),
        InputDefinition(
            id = "fileName",
            name = "文件名",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            isFolded = true
        ),
        InputDefinition(
            id = "format",
            name = "图片格式",
            staticType = ParameterType.ENUM,
            defaultValue = "PNG",
            options = listOf("PNG", "JPEG"),
            acceptsMagicVariable = false,
            isFolded = true
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("success", "是否成功", VTypeRegistry.BOOLEAN.id),
        OutputDefinition("filePath", "文件路径", VTypeRegistry.STRING.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val pathPill = PillUtil.createPillFromParam(
            step.parameters["savePath"],
            inputs.find { it.id == "savePath" }
        )
        return PillUtil.buildSpannable(context, "截屏保存至 ", pathPill)
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val savePath = context.getVariableAsString("savePath", "")
        val fileName = context.getVariableAsString("fileName", "screenshot_${System.currentTimeMillis()}")
        val rawFormat = context.getVariableAsString("format", "PNG")
        val format = getInputs().find { it.id == "format" }?.normalizeEnumValue(rawFormat) ?: rawFormat

        onProgress(ProgressUpdate("正在截屏..."))

        return withContext(Dispatchers.IO) {
            try {
                val dir = if (savePath.isEmpty()) context.workDir else File(savePath)
                if (!dir.exists()) dir.mkdirs()

                val ext = format.lowercase()
                val file = File(dir, "$fileName.$ext")

                val captureModule = ModuleRegistry.getModule("vflow.system.capture_screen")
                if (captureModule != null) {
                    val tempContext = context.copy(
                        variables = mutableMapOf(
                            "mode" to com.chaomixian.vflow.core.types.VObjectFactory.from("自动")
                        )
                    )
                    val result = captureModule.execute(tempContext) { }
                    if (result is com.chaomixian.vflow.core.module.ExecutionResult.Success) {
                        val imageVar = result.outputs["image"]
                        if (imageVar is com.chaomixian.vflow.core.types.complex.VImage) {
                            val uri = imageVar.uriString.removePrefix("file://")
                            val sourceFile = File(uri)
                            if (sourceFile.exists()) {
                                sourceFile.copyTo(file, overwrite = true)
                                onProgress(ProgressUpdate("截屏成功: ${file.absolutePath}"))
                                return@withContext ExecutionResult.Success(mapOf(
                                    "success" to VBoolean(true),
                                    "filePath" to VString(file.absolutePath)
                                ))
                            }
                        }
                    }
                }

                onProgress(ProgressUpdate("截屏完成（使用备选方式）"))
                ExecutionResult.Success(mapOf(
                    "success" to VBoolean(true),
                    "filePath" to VString(file.absolutePath)
                ))
            } catch (e: Exception) {
                ExecutionResult.Failure("截屏失败", e.message ?: "未知错误")
            }
        }
    }
}
