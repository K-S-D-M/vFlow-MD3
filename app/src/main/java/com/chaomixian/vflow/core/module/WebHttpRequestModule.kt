package com.chaomixian.vflow.core.module

import android.content.Context
import com.chaomixian.vflow.R
import com.chaomixian.vflow.core.execution.ExecutionContext
import com.chaomixian.vflow.core.types.VTypeRegistry
import com.chaomixian.vflow.core.types.basic.VNumber
import com.chaomixian.vflow.core.types.basic.VString
import com.chaomixian.vflow.core.workflow.model.ActionStep
import com.chaomixian.vflow.ui.workflow_editor.PillUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebHttpRequestModule : BaseModule() {

    companion object {
        private const val METHOD_GET = "GET"
        private const val METHOD_POST = "POST"
        private const val METHOD_PUT = "PUT"
        private const val METHOD_DELETE = "DELETE"
    }

    override val id = "vflow.automation.http_request"
    override val metadata = ActionMetadata(
        name = "HTTP请求",
        description = "支持 GET/POST 等 HTTP 请求，获取响应状态码和内容。",
        iconRes = R.drawable.rounded_public_24,
        category = "网络",
        categoryId = "network"
    )
    override val aiMetadata = directToolMetadata(
        riskLevel = AiModuleRiskLevel.HIGH,
        directToolDescription = "Send an HTTP request (GET/POST/PUT/DELETE) and get the response.",
        workflowStepDescription = "Send an HTTP request.",
        inputHints = mapOf(
            "url" to "The URL to send the request to.",
            "method" to "HTTP method: GET, POST, PUT, or DELETE.",
            "headers" to "Request headers in JSON format.",
            "body" to "Request body (for POST/PUT)."
        ),
        requiredInputIds = setOf("url", "method")
    )

    private val methodOptions = listOf(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE)

    override fun getInputs(): List<InputDefinition> = listOf(
        InputDefinition(
            id = "url",
            name = "URL",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true
        ),
        InputDefinition(
            id = "method",
            name = "请求方法",
            staticType = ParameterType.ENUM,
            defaultValue = METHOD_GET,
            options = methodOptions,
            acceptsMagicVariable = false
        ),
        InputDefinition(
            id = "headers",
            name = "请求头(JSON)",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true,
            isFolded = true
        ),
        InputDefinition(
            id = "body",
            name = "请求体",
            staticType = ParameterType.STRING,
            defaultValue = "",
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.STRING.id),
            supportsRichText = true,
            visibility = InputVisibility.notEquals("method", METHOD_GET),
            isFolded = true
        ),
        InputDefinition(
            id = "timeout",
            name = "超时时间(秒)",
            staticType = ParameterType.NUMBER,
            defaultValue = 30.0,
            acceptsMagicVariable = true,
            acceptedMagicVariableTypes = setOf(VTypeRegistry.NUMBER.id),
            isFolded = true
        )
    )

    override fun getOutputs(step: ActionStep?): List<OutputDefinition> = listOf(
        OutputDefinition("statusCode", "状态码", VTypeRegistry.NUMBER.id),
        OutputDefinition("responseBody", "响应内容", VTypeRegistry.STRING.id)
    )

    override fun getSummary(context: Context, step: ActionStep): CharSequence {
        val inputs = getInputs()
        val methodPill = PillUtil.createPillFromParam(
            step.parameters["method"],
            inputs.find { it.id == "method" },
            isModuleOption = true
        )
        val urlPill = PillUtil.createPillFromParam(
            step.parameters["url"],
            inputs.find { it.id == "url" }
        )
        return PillUtil.buildSpannable(context, methodPill, " ", urlPill)
    }

    override suspend fun execute(
        context: ExecutionContext,
        onProgress: suspend (ProgressUpdate) -> Unit
    ): ExecutionResult {
        val url = context.getVariableAsString("url", "")
        val rawMethod = context.getVariableAsString("method", METHOD_GET)
        val method = getInputs().find { it.id == "method" }?.normalizeEnumValue(rawMethod) ?: rawMethod
        val headersJson = context.getVariableAsString("headers", "")
        val body = context.getVariableAsString("body", "")
        val timeoutSec = context.getVariableAsLong("timeout") ?: 30

        if (url.isEmpty()) {
            return ExecutionResult.Failure("参数错误", "URL 不能为空")
        }

        onProgress(ProgressUpdate("正在发送 $method 请求: $url"))

        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                    .readTimeout(timeoutSec, TimeUnit.SECONDS)
                    .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                    .build()

                val requestBuilder = Request.Builder().url(url)

                if (headersJson.isNotEmpty()) {
                    try {
                        val gson = com.google.gson.Gson()
                        val headersMap = gson.fromJson(headersJson, Map::class.java) as Map<String, String>
                        headersMap.forEach { (key, value) ->
                            requestBuilder.addHeader(key, value)
                        }
                    } catch (_: Exception) {
                        requestBuilder.addHeader("Content-Type", "application/json")
                    }
                }

                when (method) {
                    METHOD_GET -> requestBuilder.get()
                    METHOD_POST -> {
                        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                        val requestBody = body.toRequestBody(mediaType)
                        requestBuilder.post(requestBody)
                    }
                    METHOD_PUT -> {
                        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                        val requestBody = body.toRequestBody(mediaType)
                        requestBuilder.put(requestBody)
                    }
                    METHOD_DELETE -> {
                        if (body.isNotEmpty()) {
                            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                            val requestBody = body.toRequestBody(mediaType)
                            requestBuilder.delete(requestBody)
                        } else {
                            requestBuilder.delete()
                        }
                    }
                }

                val response = client.newCall(requestBuilder.build()).execute()
                val responseBody = response.body?.string() ?: ""
                val statusCode = response.code

                onProgress(ProgressUpdate("请求完成，状态码: $statusCode"))
                ExecutionResult.Success(mapOf(
                    "statusCode" to VNumber(statusCode.toDouble()),
                    "responseBody" to VString(responseBody)
                ))
            } catch (e: Exception) {
                ExecutionResult.Failure("请求失败", e.message ?: "未知错误")
            }
        }
    }
}
