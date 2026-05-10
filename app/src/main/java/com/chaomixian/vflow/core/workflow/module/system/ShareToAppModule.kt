package com.chaomixian.vflow.core.workflow.module.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.module.*
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.types.basic.VNull
import com.chaomixian.vflow.core.types.basic.VString
import com.chaomixian.vflow.core.types.complex.VImage
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.app_picker.AppUserSupport
import com.chaomixian.vflow.ui.workflow_editor.PillUtil

class ShareToAppModule : BaseModule() {

    override val id = "vflow.system.share_to_app"
    override val metadata = ActionMetadata(
        nameStringRes = R.string.module_vflow_system_share_to_app_name,
        descriptionStringRes = R.string.module_vflow_system_share_to_app_desc,
        name = "分享到指定APP",
        description = "将图片或文件分享到指定的APP，支持任何支持接收分享的应用",
        iconRes = R.drawable.rounded_ios_share_24,
        category = "应用与系统",
        categoryId = "device"
    )

    override val uiProvider: ModuleUIProvider = ShareToAppUIProvider()

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "packageName",
            nameStringRes = R.string.param_vflow_system_share_to_app_package_name,
            name = "目标APP包名",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "content",
            nameStringRes = R.string.param_vflow_system_share_to_app_content_name,
            name = "分享内容",
            staticType = ParameterType.ANY,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.IMAGE.id, VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "contentType",
            nameStringRes = R.string.param_vflow_system_share_to_app_content_type_name,
            name = "内容类型",
            staticType = ParameterType.ENUM,
            defaultValue = "image",
            options = listOf("image", "text", "file"),
            optionsStringRes = listOf(
                R.string.option_vflow_system_share_to_app_type_image,
                R.string.option_vflow_system_share_to_app_type_text,
                R.string.option_vflow_system_share_to_app_type_file
            )
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition(
            id = "success",
            nameStringRes = R.string.output_vflow_system_share_to_app_success_name,
            name = "是否成功",
            typeName = VTypeRegistry.BOOLEAN.id
        )
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val packageName = step.parameters["packageName"] as? String
        
        if (packageName.isNullOrEmpty()) {
            return context.getString(R.string.summary_vflow_system_share_to_app_select_app)
        }
        
        val appName = AppUserSupport.loadAppLabel(context, packageName) ?: packageName
        
        return PillUtil.buildSpannable(
            context,
            context.getString(R.string.summary_vflow_system_share_to_app_prefix),
            PillUtil.Pill(appName, "packageName")
        )
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val packageName = context.getVariableAsString("packageName", "")
        val rawContent = context.getVariable("content")
        val contentType = context.getVariableAsString("contentType", "image") ?: "image"

        if (packageName.isBlank()) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_app_empty_package),
                appContext.getString(R.string.error_vflow_system_share_to_app_package_required)
            )
        }

        if (rawContent is VNull || rawContent == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_app_empty_content),
                appContext.getString(R.string.error_vflow_system_share_to_app_content_required)
            )
        }

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_app_preparing, packageName)))

        val intent = createShareIntent(packageName, rawContent, contentType)
        if (intent == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_app_create_intent_failed),
                appContext.getString(R.string.error_vflow_system_share_to_app_invalid_content)
            )
        }

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_app_opening)))

        try {
            if (intent.resolveActivity(appContext.packageManager) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(intent)
                onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_app_success)))
                return ExecutionResult.Success(outputs = mapOf("success" to VBoolean(true)))
            } else {
                return ExecutionResult.Failure(
                    appContext.getString(R.string.error_vflow_system_share_to_app_not_found),
                    appContext.getString(R.string.error_vflow_system_share_to_app_app_not_installed)
                )
            }
        } catch (e: Exception) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_app_share_failed),
                e.localizedMessage ?: appContext.getString(R.string.error_vflow_system_share_to_app_unknown_error)
            )
        }
    }

    private fun createShareIntent(packageName: String, content: Any, contentType: String): Intent? {
        return try {
            when (contentType) {
                "image" -> createImageIntent(packageName, content)
                "text" -> createTextIntent(packageName, content)
                "file" -> createFileIntent(packageName, content)
                else -> createImageIntent(packageName, content)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createImageIntent(packageName: String, content: Any): Intent? {
        val uri = when (content) {
            is VImage -> {
                val path = content.value as? String ?: return null
                val file = java.io.File(path)
                if (!file.exists()) return null
                getFileUri(file)
            }
            is VString -> {
                val path = content.value as? String ?: return null
                val file = java.io.File(path)
                if (!file.exists()) return null
                getFileUri(file)
            }
            else -> null
        } ?: return null

        return Intent(Intent.ACTION_SEND).apply {
            setPackage(packageName)
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun createTextIntent(packageName: String, content: Any): Intent {
        val text = content.toString()
        return Intent(Intent.ACTION_SEND).apply {
            setPackage(packageName)
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    private fun createFileIntent(packageName: String, content: Any): Intent? {
        val uri = when (content) {
            is VImage -> {
                val path = content.value as? String ?: return null
                val file = java.io.File(path)
                if (!file.exists()) return null
                getFileUri(file)
            }
            is VString -> {
                val path = content.value as? String ?: return null
                val file = java.io.File(path)
                if (!file.exists()) return null
                getFileUri(file)
            }
            else -> null
        } ?: return null

        val mimeType = getMimeType(uri.toString())

        return Intent(Intent.ACTION_SEND).apply {
            setPackage(packageName)
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun getFileUri(file: java.io.File): Uri? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                androidx.core.content.FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMimeType(path: String): String {
        val extension = path.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "image/*"
            "mp4", "avi", "mov", "mkv" -> "video/*"
            "mp3", "wav", "flac", "aac", "ogg" -> "audio/*"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "txt" -> "text/plain"
            "zip", "rar", "7z" -> "application/zip"
            else -> "*/*"
        }
    }
}