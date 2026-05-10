package com.chaomixian.vflow.core.workflow.module.system

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.module.*
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VBoolean
import com.chaomixian.vflow.core.types.basic.VString
import com.chaomixian.vflow.core.types.complex.VImage
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.services.ExecutionUIService
import com.chaomixian.vflow.ui.app_picker.AppUserSupport
import com.chaomixian.vflow.ui.workflow_editor.PillUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AppPhotoCaptureModule : BaseModule() {

    companion object {
        const val REQUEST_TAKE_PHOTO = 10010
    }

    override val id = "vflow.system.app_photo_capture"
    override val metadata = ActionMetadata(
        nameStringRes = R.string.module_vflow_system_app_photo_capture_name,
        descriptionStringRes = R.string.module_vflow_system_app_photo_capture_desc,
        name = "启动APP拍照",
        description = "打开指定APP的拍照功能，拍照完成后保存照片到文件",
        iconRes = R.drawable.rounded_camera_alt_24,
        category = "应用与系统",
        categoryId = "device"
    )

    override val uiProvider: ModuleUIProvider = AppPhotoCaptureUIProvider()

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "packageName",
            nameStringRes = R.string.param_vflow_system_app_photo_capture_package_name,
            name = "APP包名",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "activityName",
            nameStringRes = R.string.param_vflow_system_app_photo_capture_activity_name,
            name = "拍照Activity",
            staticType = ParameterType.STRING,
            defaultValue = "LAUNCH",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "outputPath",
            nameStringRes = R.string.param_vflow_system_app_photo_capture_output_path,
            name = "保存路径",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id)
        ),
        InputDefinition(
            id = "waitForPhoto",
            nameStringRes = R.string.param_vflow_system_app_photo_capture_wait_name,
            name = "等待拍照完成",
            staticType = ParameterType.BOOLEAN,
            defaultValue = true
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition(
            id = "photoUri",
            nameStringRes = R.string.output_vflow_system_app_photo_capture_photo_uri_name,
            name = "照片URI",
            typeName = VTypeRegistry.STRING.id
        ),
        OutputDefinition(
            id = "photoPath",
            nameStringRes = R.string.output_vflow_system_app_photo_capture_photo_path_name,
            name = "照片路径",
            typeName = VTypeRegistry.STRING.id
        ),
        OutputDefinition(
            id = "photo",
            nameStringRes = R.string.output_vflow_system_app_photo_capture_photo_name,
            name = "照片",
            typeName = VTypeRegistry.IMAGE.id
        ),
        OutputDefinition(
            id = "success",
            nameStringRes = R.string.output_vflow_system_app_photo_capture_success_name,
            name = "是否成功",
            typeName = VTypeRegistry.BOOLEAN.id
        )
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val packageName = step.parameters["packageName"] as? String
        
        if (packageName.isNullOrEmpty()) {
            return context.getString(R.string.summary_vflow_system_app_photo_capture_select_app)
        }
        
        val appName = AppUserSupport.loadAppLabel(context, packageName) ?: packageName
        
        return PillUtil.buildSpannable(
            context,
            context.getString(R.string.summary_vflow_system_app_photo_capture_prefix),
            PillUtil.Pill(appName, "packageName")
        )
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val packageName = context.getVariableAsString("packageName", "")
        val activityName = context.getVariableAsString("activityName", "LAUNCH")
        val customOutputPath = context.getVariableAsString("outputPath", "")
        val waitForPhoto = context.getVariableAsBoolean("waitForPhoto") ?: true

        if (packageName.isBlank()) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_empty_package),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_package_required)
            )
        }

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_app_photo_capture_preparing, packageName)))

        val photoFile = createImageFile(customOutputPath)
        val photoUri = getUriForFile(photoFile)

        if (photoUri == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_uri_failed),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_create_uri_failed)
            )
        }

        val captureIntent = createCaptureIntent(packageName, activityName, photoUri)
        if (captureIntent == null) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_intent_failed),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_create_intent_failed)
            )
        }

        val uiService = context.services.get(ExecutionUIService::class)
            ?: return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_service_missing),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_no_ui_service)
            )

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_app_photo_capture_opening_app)))

        val launchSuccess = uiService.startActivityForResult(captureIntent, REQUEST_TAKE_PHOTO)
        if (launchSuccess != true) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_launch_failed),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_cannot_launch_app)
            )
        }

        if (waitForPhoto) {
            onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_app_photo_capture_waiting)))
            
            val waitResult = uiService.waitForActivityResult(REQUEST_TAKE_PHOTO, 60000)
            if (waitResult != Activity.RESULT_OK) {
                photoFile.delete()
                return ExecutionResult.Failure(
                    appContext.getString(R.string.error_vflow_system_app_photo_capture_cancelled),
                    appContext.getString(R.string.error_vflow_system_app_photo_capture_user_cancelled)
                )
            }
        }

        if (!photoFile.exists() || photoFile.length() == 0L) {
            return ExecutionResult.Failure(
                appContext.getString(R.string.error_vflow_system_app_photo_capture_no_photo),
                appContext.getString(R.string.error_vflow_system_app_photo_capture_file_not_found)
            )
        }

        val photoPath = photoFile.absolutePath
        val photoUriStr = photoUri.toString()

        onProgress(ProgressUpdate(appContext.getString(R.string.msg_vflow_system_app_photo_capture_success, photoPath)))

        return ExecutionResult.Success(
            outputs = mapOf(
                "photoUri" to VString(photoUriStr),
                "photoPath" to VString(photoPath),
                "photo" to VImage(photoPath),
                "success" to VBoolean(true)
            )
        )
    }

    private fun createImageFile(customPath: String?): File {
        return if (!customPath.isNullOrBlank()) {
            val customFile = File(customPath)
            if (customFile.parentFile?.exists() != true) {
                customFile.parentFile?.mkdirs()
            }
            File(customFile.absolutePath)
        } else {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            File.createTempFile(imageFileName, ".jpg", storageDir)
        }
    }

    private fun getUriForFile(file: File): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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

    private fun createCaptureIntent(packageName: String, activityName: String, outputUri: Uri): Intent? {
        return try {
            val intent = if (activityName == "LAUNCH" || activityName.isBlank()) {
                val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent == null) return null
                launchIntent
            } else {
                Intent().apply {
                    component = ComponentName(packageName, activityName)
                }
            }
            
            intent.apply {
                action = MediaStore.ACTION_IMAGE_CAPTURE
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            null
        }
    }
}