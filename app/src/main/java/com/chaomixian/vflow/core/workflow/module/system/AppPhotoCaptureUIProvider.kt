package com.chaomixian.vflow.core.workflow.module.system

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.module.CustomEditorViewHolder
import com.chaomixian.vflow.core.module.ModuleUIProvider
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.app_picker.AppPickerMode
import com.chaomixian.vflow.ui.app_picker.AppUserSupport
import com.chaomixian.vflow.ui.app_picker.UnifiedAppPickerSheet

class AppPhotoCaptureViewHolder(
    view: View,
    val summaryTextView: TextView,
    val pickButton: Button
) : CustomEditorViewHolder(view) {
    var selectedPackageName: String? = null
    var selectedActivityName: String? = null
}

class AppPhotoCaptureUIProvider : ModuleUIProvider {

    override fun getHandledInputIds(): Set<String> = setOf("packageName", "activityName")

    override fun createEditor(
        context: Context,
        parent: ViewGroup,
        currentParameters: Map<String, Any?>,
        onParametersChanged: () -> Unit,
        onMagicVariableRequested: ((String) -> Unit)?,
        allSteps: List<ActionStep>?,
        onStartActivityForResult: ((Intent, (Int, Intent?) -> Unit) -> Unit)?
    ): CustomEditorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.partial_app_photo_capture_editor, parent, false)
        val holder = AppPhotoCaptureViewHolder(
            view,
            view.findViewById(R.id.text_selected_app_summary),
            view.findViewById(R.id.button_pick_app)
        )

        holder.selectedPackageName = currentParameters["packageName"] as? String
        holder.selectedActivityName = currentParameters["activityName"] as? String
        updateSummaryText(context, holder.summaryTextView, currentParameters)

        holder.pickButton.setOnClickListener {
            val intent = Intent().apply {
                putExtra(UnifiedAppPickerSheet.EXTRA_MODE, AppPickerMode.SELECT_ACTIVITY.name)
            }
            onStartActivityForResult?.invoke(intent) { resultCode, data ->
                if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                    val packageName = data.getStringExtra(UnifiedAppPickerSheet.EXTRA_SELECTED_PACKAGE_NAME)
                    val activityName = data.getStringExtra(UnifiedAppPickerSheet.EXTRA_SELECTED_ACTIVITY_NAME)
                    if (packageName != null) {
                        holder.selectedPackageName = packageName
                        holder.selectedActivityName = activityName
                        updateSummaryText(context, holder.summaryTextView, mapOf(
                            "packageName" to packageName,
                            "activityName" to activityName
                        ))
                        onParametersChanged()
                    }
                }
            }
        }

        return holder
    }

    override fun readFromEditor(holder: CustomEditorViewHolder): Map<String, Any?> {
        val photoCaptureHolder = holder as? AppPhotoCaptureViewHolder ?: return emptyMap()
        return mapOf(
            "packageName" to (photoCaptureHolder.selectedPackageName as Any?),
            "activityName" to (photoCaptureHolder.selectedActivityName as Any?)
        ).filterValues { it != null }
    }

    private fun updateSummaryText(context: Context, textView: TextView, parameters: Map<String, Any?>) {
        val packageName = parameters["packageName"] as? String
        val activityName = parameters["activityName"] as? String

        if (packageName.isNullOrEmpty()) {
            textView.text = context.getString(R.string.text_not_selected)
            return
        }

        val appName = AppUserSupport.loadAppLabel(context, packageName) ?: packageName
        val displayText = if (activityName == "LAUNCH" || activityName.isNullOrEmpty()) {
            context.getString(R.string.text_app_selected, appName)
        } else {
            val simpleActivityName = activityName.substringAfterLast('.')
            context.getString(R.string.text_activity_selected, appName, simpleActivityName)
        }
        textView.text = displayText
    }

    override fun createPreview(
        context: Context, parent: ViewGroup, step: ActionStep, allSteps: List<ActionStep>,
        onStartActivityForResult: ((Intent, (resultCode: Int, data: Intent?) -> Unit) -> Unit)?
    ): View? = null
}