// 文件: main/java/com/chaomixian/vflow/core/module/ModuleRegistry.kt
package com.chaomixian.vflow.core.module

import android.content.Context
import android.content.ContentValues.TAG
import com.chaomixian.vflow.core.logging.DebugLogger
import com.chaomixian.vflow.core.workflow.module.data.*
import com.chaomixian.vflow.core.workflow.module.file.*
import com.chaomixian.vflow.core.workflow.module.interaction.*
import com.chaomixian.vflow.core.workflow.module.logic.*
import com.chaomixian.vflow.core.workflow.module.network.*
import com.chaomixian.vflow.core.workflow.module.notification.*
import com.chaomixian.vflow.core.workflow.module.shizuku.*
import com.chaomixian.vflow.core.workflow.module.system.*
import com.chaomixian.vflow.core.workflow.module.triggers.*
import com.chaomixian.vflow.core.workflow.module.triggers.TimeTriggerModule as LegacyTimeTriggerModule
import com.chaomixian.vflow.core.workflow.module.triggers.BatteryTriggerModule as LegacyBatteryTriggerModule
import com.chaomixian.vflow.core.workflow.module.snippet.*
import com.chaomixian.vflow.core.workflow.module.ui.blocks.*
import com.chaomixian.vflow.core.workflow.module.ui.components.*
import com.chaomixian.vflow.core.workflow.module.core.*

object ModuleRegistry {
    private val modules = mutableMapOf<String, ActionModule>()
    private var isCoreInitialized = false

    fun register(module: ActionModule, context: Context? = null) {
        if (modules.containsKey(module.id)) {
            DebugLogger.w(TAG,"警告: 模块ID '${module.id}' 被重复注册。")
        }
        modules[module.id] = module

        if (context != null && module is BaseModule) {
            module.initContext(context)
        }
    }

    fun getModule(id: String): ActionModule? = modules[id]
    fun getAllModules(): List<ActionModule> = modules.values.toList()

    fun getModulesByCategory(): Map<String, List<ActionModule>> {
        return modules.values
            .filter { it.blockBehavior.type != BlockType.BLOCK_END && it.blockBehavior.type != BlockType.BLOCK_MIDDLE }
            .groupBy { it.metadata.getResolvedCategoryId() }
            .toSortedMap(compareBy { ModuleCategories.getSortOrder(it) })
    }

    fun reset() {
        modules.clear()
        isCoreInitialized = false
    }

    fun initialize(context: Context) {
        if (isCoreInitialized) return

        modules.clear()

        // 触发器
        register(ManualTriggerModule(), context)
        register(ReceiveShareTriggerModule(), context)
        register(AppStartTriggerModule(), context)
        register(AppPackageTriggerModule(), context)
        register(ClipboardTriggerModule(), context)
        register(KeyEventTriggerModule(), context)
        register(BackTapTriggerModule(), context)
        register(LegacyTimeTriggerModule(), context)
        register(IntervalTriggerModule(), context)
        register(LegacyBatteryTriggerModule(), context)
        register(PowerTriggerModule(), context)
        register(ScreenTriggerModule(), context)
        register(WifiTriggerModule(), context)
        register(BluetoothTriggerModule(), context)
        register(SmsTriggerModule(), context)
        register(CallTriggerModule(), context)
        register(NotificationTriggerModule(), context)
        register(ElementTriggerModule(), context)
        register(GKDTriggerModule(), context)
        register(LocationTriggerModule(), context)
        register(PoseTriggerModule(), context)
        register(VoiceTriggerModule(), context)

        // 新版触发器模块 (core.module 包)
        register(TimeTriggerModule(), context)
        register(BatteryTriggerModule(), context)

        // 界面交互
        register(FindTextModule(), context)
        register(FindElementModule(), context)
        register(UiSelectorModule(), context)
        register(ClickModule(), context)
        register(ScreenOperationModule(), context)
        register(SendKeyEventModule(), context)
        register(InputTextModule(), context)
        register(CaptureScreenModule(), context)
        register(OCRModule(), context)
        register(AgentModule(), context)
        register(AutoGLMModule(), context)
        register(FindTextUntilModule(), context)
        register(FindImageModule(), context)
        register(OperitModule(), context)
        register(GetCurrentActivityModule(), context)
        register(WaitForElementModule(), context)

        // 逻辑控制
        register(IfModule(), context)
        register(ElseModule(), context)
        register(EndIfModule(), context)
        register(LoopModule(), context)
        register(EndLoopModule(), context)
        register(ForEachModule(), context)
        register(EndForEachModule(), context)
        register(JumpModule(), context)
        register(WhileModule(), context)
        register(EndWhileModule(), context)
        register(BreakLoopModule(), context)
        register(ContinueLoopModule(), context)
        register(StopWorkflowModule(), context)
        register(CallWorkflowModule(), context)
        register(StopAndReturnModule(), context)

        // 数据
        register(CreateVariableModule(), context)
        register(LoadVariablesModule(), context)
        register(GetCurrentTimeModule(), context)
        register(RandomVariableModule(), context)
        register(ModifyVariableModule(), context)
        register(GetVariableModule(), context)
        register(CalculationModule(), context)
        register(TextProcessingModule(), context)
        register(TextSplitModule(), context)
        register(TextReplaceModule(), context)
        register(TextExtractModule(), context)
        register(Base64EncodeOrDecodeModule(), context)
        register(HashModule(), context)
        register(UrlEncodeOrDecodeModule(), context)
        register(AesCryptoModule(), context)
        register(DesCryptoModule(), context)
        register(Rc4CryptoModule(), context)
        register(Sm4CryptoModule(), context)
        register(ParseJsonModule(), context)
        register(ParseXmlModule(), context)
        register(CommentModule(), context)
        register(FileOperationModule(), context)

        // 文件
        register(ImportImageModule(), context)
        register(SaveImageModule(), context)
        register(AdjustImageModule(), context)
        register(ScaleImageModule(), context)
        register(RotateImageModule(), context)
        register(ApplyMaskModule(), context)
        register(FileOpsModule(), context)

        // 网络
        register(GetIpAddressModule(), context)
        register(HttpRequestModule(), context)
        register(BarkPushModule(), context)
        register(DiscordPushModule(), context)
        register(WebhookPushModule(), context)
        register(TelegramPushModule(), context)
        register(AIModule(), context)
        register(FeishuSendMessageModule(), context)
        register(FeishuGetMessageHistoryModule(), context)
        register(FeishuMediaUploadModule(), context)
        register(WebHttpRequestModule(), context)

        // 应用与系统
        register(DelayModule(), context)
        register(InputModule(), context)
        register(SpeechToTextModule(), context)
        register(QuickViewModule(), context)
        register(ToastModule(), context)
        register(LuaModule(), context)
        register(JsModule(), context)
        register(FindInstalledAppModule(), context)
        register(LaunchAppModule(), context)
        register(CloseAppModule(), context)
        register(GetClipboardModule(), context)
        register(SetClipboardModule(), context)
        register(ShareModule(), context)
        register(SendNotificationModule(), context)
        register(WifiModule(), context)
        register(BluetoothModule(), context)
        register(BrightnessModule(), context)
        register(MobileDataModule(), context)
        register(GetScreenStateModule(), context)
        register(WakeScreenModule(), context)
        register(WakeAndUnlockScreenModule(), context)
        register(SleepScreenModule(), context)
        register(ReadSmsModule(), context)
        register(FindNotificationModule(), context)
        register(RemoveNotificationModule(), context)
        register(GetAppUsageStatsModule(), context)
        register(InvokeModule(), context)
        register(SystemInfoModule(), context)
        register(LocationRangeCheckModule(), context)
        register(PlayAudioModule(), context)
        register(TextToSpeechModule(), context)
        register(CallPhoneModule(), context)
        register(DarkModeModule(), context)
        register(VibrationModule(), context)
        register(FlashlightModule(), context)
        register(PushNotificationModule(), context)
        register(DeviceStatusModule(), context)
        register(AppLauncherModule(), context)
        register(AutoScreenshotModule(), context)

        // 拍照与分享模块
        register(AppPhotoCaptureModule(), context)
        register(ShareToWeChatModule(), context)
        register(ShareToAppModule(), context)

        // Core (Beta) 模块
        register(CoreBluetoothModule(), context)
        register(CoreBluetoothStateModule(), context)
        register(CoreWifiModule(), context)
        register(CoreWifiStateModule(), context)
        register(CoreNfcModule(), context)
        register(CoreNfcStateModule(), context)
        register(CoreSetClipboardModule(), context)
        register(CoreGetClipboardModule(), context)
        register(CoreWakeScreenModule(), context)
        register(CoreSleepScreenModule(), context)
        register(CoreScreenStatusModule(), context)
        register(CoreCaptureScreenModule(), context)
        register(CoreScreenOperationModule(), context)
        register(CoreUinputScreenOperationModule(), context)
        register(CoreInputTextModule(), context)
        register(CorePressKeyModule(), context)
        register(CoreTouchReplayModule(), context)
        register(CoreForceStopAppModule(), context)
        register(CoreShellCommandModule(), context)
        register(CoreVolumeModule(), context)
        register(CoreVolumeStateModule(), context)

        // Shizuku 模块
        register(ShellCommandModule(), context)
        register(AlipayShortcutsModule(), context)
        register(WeChatShortcutsModule(), context)
        register(ColorOSShortcutsModule(), context)
        register(GeminiAssistantModule(), context)

        // Snippet 模板
        register(FindTextUntilSnippet(), context)

        // UI 组件模块
        register(CreateActivityModule(), context)
        register(ShowActivityModule(), context)
        register(EndActivityModule(), context)
        register(CreateFloatWindowModule(), context)
        register(ShowFloatWindowModule(), context)
        register(EndFloatWindowModule(), context)

        register(UiTextModule(), context)
        register(UiInputModule(), context)
        register(UiButtonModule(), context)
        register(UiSwitchModule(), context)

        register(OnUiEventModule(), context)
        register(EndOnUiEventModule(), context)
        register(UpdateUiComponentModule(), context)
        register(GetComponentValueModule(), context)
        register(ExitActivityModule(), context)

        isCoreInitialized = true
    }
}
