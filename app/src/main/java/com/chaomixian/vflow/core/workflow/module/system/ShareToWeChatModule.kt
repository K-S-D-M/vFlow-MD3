package com.chaomixian.vflow.core.workflow.module.system

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
import com.chaomixian.vflow.ui.workflow_editor.PillUtil

class ShareToWeChatModule : BaseModule() {

    companion object {
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val WECHAT_LAUNCHER_ACTIVITY = "com.tencent.mm.ui.LauncherUI"
    }

    override val id = "vflow.system.share_to_wechat"
    override val metadata = ActionMetadata(
        nameStringRes = R.string.module_vflow_system_share_to_wechat_name,
        descriptionStringRes = R.string.module_vflow_system_share_to_wechat_desc,
        name = "分享到微信",
        description = "将图片或文件分享到微信好友",
        iconRes = R.drawable.rounded_share_24,
        category = "应用与系统",
        categoryId = "device"
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "content",
            nameStringRes = R.string.param_vflow_system_share_to_wechat_content_name,
            name = "分享内容",
            staticType = ParameterType.ANY,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.IMAGE.id, VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "contentType",
            nameStringRes = R.string.param_vflow_system_share_to_wechat_content_type_name,
            name = "内容类型",
            staticType = ParameterType.ENUM,
            defaultValue = "image",
            options = listOf("image", "text"),
            optionsStringRes = listOf(
                R.string.option_vflow_system_share_to_wechat_type_image,
                R.string.option_vflow_system_share_to_wechat_type_text
            )
        ),
        InputDefinition(
            id = "openChat",
            nameStringRes = R.string.param_vflow_system_share_to_wechat_open_chat_name,
            name = "直接打开聊天",
            staticType = ParameterType.BOOLEAN,
            defaultValue = false
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition(
            id = "success",
            nameStringRes = R.string.output_vflow_system_share_to_wechat_success_name,
            name = "是否成功",
            typeName = VTypeRegistry.BOOLEAN.id
        )
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val contentType = step.parameters["contentType"] as? String ?: "image"
        val openChat = step.parameters["openChat"] as? Boolean ?: false
        
        val typeText = if (contentType == "image") {
            context.getString(R.string.summary_vflow_system_share_to_wechat_image_suffix)
        } else {
            context.getString(R.string.summary_vflow_system_share_to_wechat_text_suffix)
        }
        
        val chatText = if (openChat) {
            context.getString(R.string.summary_vflow_system_share_to_wechat_chat_suffix)
        } else {
            ""
        }
        
        return PillUtil.buildSpannable(
            context,
            context.getString(R.string.summary_vflow_system_share_to_wechat_prefix),
            typeText,
            chatText
        )
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val rawContent = context.getVariable("content")
        val contentType = context.getVariableAsString("contentType", "image") ?: "image"
        val openChat = context.getVariableAsBoolean("openChat") ?: false

        if (rawContent is VNull || rawContent == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_wechat_empty_content),
                appContext.getString(R.string.error_vflow_system_share_to_wechat_content_required)
            )
        }

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_wechat_preparing)))

        val intent = if (contentType == "image") {
            createImageShareIntent(rawContent, openChat)
        } else {
            createTextShareIntent(rawContent, openChat)
        }

        if (intent == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_wechat_create_intent_failed),
                appContext.getString(R.string.error_vflow_system_share_to_wechat_invalid_content)
            )
        }

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_wechat_opening)))

        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
            onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_wechat_success)))
            return ExecutionResult.Success(outputs = mapOf("success" to VBoolean(true)))
        } catch (e: Exception) {
            onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_share_to_wechat_fallback)))
            return tryFallbackShare(context, rawContent, contentType)
        }
    }

    private fun createImageShareIntent(content: Any, openChat: Boolean): Intent? {
        val photoUri = when (content) {
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
            else -> return null
        }

        if (photoUri == null) return null

        return Intent().apply {
            action = Intent.ACTION_SEND
            setPackage(WECHAT_PACKAGE)
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            if (openChat) {
                component = android.content.ComponentName(WECHAT_PACKAGE, WECHAT_LAUNCHER_ACTIVITY)
            }
        }
    }

    private fun createTextShareIntent(content: Any, openChat: Boolean): Intent? {
        val text = when (content) {
            is VString -> content.value as? String
            is VImage -> (content.value as? String) ?: (content as? Any)?.toString()
            else -> content.toString()
        }

        if (text.isNullOrBlank()) return null

        return Intent().apply {
            action = Intent.ACTION_SEND
            setPackage(WECHAT_PACKAGE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            
            if (openChat) {
                component = android.content.ComponentName(WECHAT_PACKAGE, WECHAT_LAUNCHER_ACTIVITY)
            }
        }
    }

    private fun tryFallbackShare(context: ExecutionContext, content: Any, contentType: String): ExecutionResult {
        return try {
            val intent = when (contentType) {
                "image" -> {
                    val photoUri = when (content) {
                        is VImage -> {
                            val path = content.value as? String ?: return ExecutionResult.Failure(
                                appContext.getString(R.string.error_vflow_system_share_to_wechat_invalid_path),
                                appContext.getString(R.string.error_vflow_system_share_to_wechat_file_not_found)
                            )
                            val file = java.io.File(path)
                            if (!file.exists()) return ExecutionResult.Failure(
                                appContext.getString(R.string.error_vflow_system_share_to_wechat_invalid_path),
                                appContext.getString(R.string.error_vflow_system_share_to_wechat_file_not_found)
                            )
                            getFileUri(file)
                        }
                        else -> null
                    }
                    if (photoUri == null) return ExecutionResult.Failure(
                        appContext.getString(R.string.error_vflow_system_share_to_wechat_create_intent_failed),
                        appContext.getString(R.string.error_vflow_system_share_to_wechat_invalid_content)
                    )
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, photoUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                else -> {
                    val text = content.toString()
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                }
            }

            appContext.startActivity(Intent.createChooser(intent, 
                appContext.getString(R.string.text_share_via)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            
            ExecutionResult.Success(outputs = mapOf("success" to VBoolean(true)))
        } catch (e: Exception) {
            ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_share_to_wechat_share_failed),
                e.localizedMessage ?: appContext.getString(R.string.error_vflow_system_share_to_wechat_unknown_error)
            )
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
}