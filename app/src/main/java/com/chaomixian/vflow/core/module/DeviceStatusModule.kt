package com.chaomixian.vflow.core.module

import android.content.Context
import android.media.AudioManager
import android.os.BatteryManager
import android.net.wifi.WifiManager
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VNumber
import com.chaomixian.vflow.core.types.basic.VString
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.workflow_editor.PillUtil

class DeviceStatusModule : BaseModule() {

    companion object {
        private const val INFO_BATTERY_LEVEL = "battery_level"
        private const val INFO_VOLUME = "volume"
        private const val INFO_WIFI_STATUS = "wifi_status"
        private const val INFO_BATTERY_CHARGING = "battery_charging"
        private const val INFO_WIFI_SSID = "wifi_ssid"
    }

    override val id = "vflow.automation.get_system_info"
    override val metadata = ActionMetadata(
        name = "获取系统信息",
        description = "获取电池电量、音量、Wi-Fi状态等系统信息。",
        iconRes = R.drawable.rounded_info_24,
        category = "应用与系统",
        categoryId = "device"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.READ_ONLY,
        directToolDescription = "Get device system info such as battery level, volume, or Wi-Fi status.",
        workflowStepDescription = "Read a device status field.",
        inputHints = mapOf(
            "infoType" to "The type of system info to retrieve: battery_level, volume, wifi_status, battery_charging, wifi_ssid."
        ),
        requiredInputIds = setOf("infoType")
    )

    private val infoTypeOptions = listOf(
        INFO_BATTERY_LEVEL,
        INFO_VOLUME,
        INFO_WIFI_STATUS,
        INFO_BATTERY_CHARGING,
        INFO_WIFI_SSID
    )

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "infoType",
            name = "信息类型",
            staticType = ParameterType.ENUM,
            defaultValue = INFO_BATTERY_LEVEL,
            options = infoTypeOptions,
            acceptsMagicVariable = false
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("value", "信息值", VTypeRegistry.STRING.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val typePill = PillUtil.createPillFromParam(
            step.parameters["infoType"],
            inputs.find { it.id == "infoType" },
            isModuleOption = true
        )
        return PillUtil.buildSpannable(context, "获取 ", typePill)
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val rawInfoType = context.getVariableAsString("infoType", INFO_BATTERY_LEVEL)
        val infoType = getInputs().find { it.id == "infoType" }?.normalizeEnumValue(rawInfoType) ?: rawInfoType
        val appCtx = context.applicationContext

        onProgress(ProgressUpdate("正在获取系统信息: $infoType"))

        val resultValue: String = when (infoType) {
            INFO_BATTERY_LEVEL -> {
                val bm = appCtx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toString()
            }
            INFO_BATTERY_CHARGING -> {
                val bm = appCtx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                (bm.isCharging).toString()
            }
            INFO_VOLUME -> {
                val am = appCtx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                "$current/$max"
            }
            INFO_WIFI_STATUS -> {
                val wm = appCtx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                wm.isWifiEnabled.toString()
            }
            INFO_WIFI_SSID -> {
                val wm = appCtx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wm.connectionInfo
                info?.ssid?.removeSurrounding("\"") ?: "未连接"
            }
            else -> return ExecutionResult.Failure("获取失败", "无效的信息类型: $infoType")
        }

        onProgress(ProgressUpdate("获取成功: $resultValue"))
        return ExecutionResult.Success(mapOf("value" to VString(resultValue)))
    }
}
