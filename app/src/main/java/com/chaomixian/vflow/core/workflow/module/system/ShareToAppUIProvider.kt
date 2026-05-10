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

class ShareToAppViewHolder(
    view: View,
    val summaryTextView: TextView,
    val pickButton: Button
) : CustomEditorViewHolder(view) {
    var selectedPackageName: String? = null
}

class ShareToAppUIProvider : ModuleUIProvider {

    override fun getHandledInputIds(): Set<String> = setOf("packageName")

    override fun createEditor(
        context: Context,
        parent: ViewGroup,
        currentParameters: Map<String, Any?>,
        onParametersChanged: () -> Unit,
        onMagicVariableRequested: ((String) -> Unit)?,
        allSteps: List<ActionStep>?,
        onStartActivityForResult: ((Intent, (Int, Intent?) -> Unit) -> Unit)?
    ): CustomEditorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.partial_share_to_app_editor, parent, false)
        val holder = ShareToAppViewHolder(
            view,
            view.findViewById(R.id.text_selected_app_summary),
            view.findViewById(R.id.button_pick_app)
        )

        holder.selectedPackageName = currentParameters["packageName"] as? String
        updateSummaryText(context, holder.summaryTextView, currentParameters)

        holder.pickButton.setOnClickListener {
            val intent = Intent().apply {
                putExtra(UnifiedAppPickerSheet.EXTRA_MODE, AppPickerMode.SELECT_APP.name)
            }
            onStartActivityForResult?.invoke(intent) { resultCode, data ->
                if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                    val packageName = data.getStringExtra(UnifiedAppPickerSheet.EXTRA_SELECTED_PACKAGE_NAME)
                    if (packageName != null) {
                        holder.selectedPackageName = packageName
                        updateSummaryText(context, holder.summaryTextView, mapOf(
                            "packageName" to packageName
                        ))
                        onParametersChanged()
                    }
                }
            }
        }

        return holder
    }

    override fun readFromEditor(holder: CustomEditorViewHolder): Map<String, Any?> {
        val shareToAppHolder = holder as? ShareToAppViewHolder ?: return emptyMap()
        return mapOf(
            "packageName" to (shareToAppHolder.selectedPackageName as Any?)
        ).filterValues { it != null }
    }

    private fun updateSummaryText(context: Context, textView: TextView, parameters: Map<String, Any?>) {
        val packageName = parameters["packageName"] as? String

        if (packageName.isNullOrEmpty()) {
            textView.text = context.getString(R.string.text_not_selected)
            return
        }

        val appName = AppUserSupport.loadAppLabel(context, packageName) ?: packageName
        textView.text = context.getString(R.string.text_app_selected, appName)
    }

    override fun createPreview(
        context: Context, parent: ViewGroup, step: ActionStep, allSteps: List<ActionStep>,
        onStartActivityForResult: ((Intent, (resultCode: Int, data: Intent?) -> Unit) -> Unit)?
    ): View? = null
}