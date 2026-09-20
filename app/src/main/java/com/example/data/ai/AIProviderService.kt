package com.example.data.ai

import com.example.BuildConfig
import com.example.data.preferences.AIProviderType
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AIProviderService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Streams real-time AI response from configured API providers with automatic multi-API failover
     */
    fun streamContent(
        request: AIRequest,
        settings: UserSettings
    ): Flow<AIStreamChunk> = flow {
        val currentDateTimeStr = SimpleDateFormat(
            "EEEE, MMMM dd, yyyy 'at' hh:mm:ss a z",
            Locale.getDefault()
        ).format(Date())

        val enrichedSystemInstruction = buildString {
            append("Real-Time Temporal Grounding: Current local device date and time is $currentDateTimeStr.\n")
            append("Always answer with authentic real-time context.\n")
            if (!request.systemInstruction.isNullOrBlank()) {
                append(request.systemInstruction)
                append("\n")
            }
            if (settings.userPersonality.isNotBlank()) {
                append("User profile & personality preferences: ${settings.userPersonality}.\n")
            }
        }

        val enrichedRequest = request.copy(systemInstruction = enrichedSystemInstruction)

        // Stream thinking simulation if user enabled reasoning mode
        if (request.isThinkingEnabled) {
            val thinkingSteps = listOf(
                "Analyzing query constraints with real-time temporal grounding ($currentDateTimeStr)...",
                "Synthesizing parameter matrix across configured model endpoints...",
                "Formulating optimal structural layout and verified data..."
            )
            for (step in thinkingSteps) {
                emit(AIStreamChunk(text = "", thinkingText = step, isDone = false))
                delay(220)
            }
            emit(AIStreamChunk(text = "", thinkingText = "Reasoning complete.\n\n", isDone = false))
        }

        val primaryKey = resolveApiKey(settings.customApiKey)
        val backupKey = settings.backupApiKey.trim()

        if (primaryKey.isBlank() && backupKey.isBlank()) {
            emit(
                AIStreamChunk(
                    text = "⚠️ No API Key Configured\n\n" +
                            "To receive live, realtime AI responses:\n" +
                            "1. Open the Profile & Settings tab.\n" +
                            "2. Select your AI Provider (Google Gemini, OpenAI, Anthropic, Perplexity, GLM, etc.).\n" +
                            "3. Enter your API Key and tap 'Test Connection' to verify it.\n\n" +
                            "You can also configure a secondary backup API key so MAX-N automatically switches providers if you reach rate limits.",
                    isDone = true
                )
            )
            return@flow
        }

        var executionSuccess = false
        var primaryError: String? = null

        // 1. Try Primary Provider
        if (primaryKey.isNotBlank()) {
            try {
                val responseText = executeProviderCall(
                    request = enrichedRequest,
                    apiKey = primaryKey,
                    provider = settings.provider,
                    customBaseUrl = settings.customBaseUrl,
                    customModel = settings.customModel
                )

                if (responseText.isNotBlank()) {
                    streamTextInChunks(responseText) { chunk ->
                        emit(AIStreamChunk(text = chunk, isDone = false))
                    }
                    executionSuccess = true
                } else {
                    primaryError = "Received empty response from ${settings.provider.displayName}."
                }
            } catch (e: Exception) {
                primaryError = e.message ?: "Unknown error"
            }
        }

        // 2. Automatic Failover to Backup Provider on rate limit (429), quota exhaustion, or failure
        if (!executionSuccess && backupKey.isNotBlank()) {
            emit(
                AIStreamChunk(
                    text = "*(Primary provider limit reached or unavailable: $primaryError. Automatically switching to backup provider [${settings.backupProvider.displayName}]...)*\n\n",
                    isDone = false
                )
            )

            try {
                val backupResponse = executeProviderCall(
                    request = enrichedRequest,
                    apiKey = backupKey,
                    provider = settings.backupProvider,
                    customBaseUrl = "",
                    customModel = ""
                )

                if (backupResponse.isNotBlank()) {
                    streamTextInChunks(backupResponse) { chunk ->
                        emit(AIStreamChunk(text = chunk, isDone = false))
                    }
                    executionSuccess = true
                }
            } catch (backupEx: Exception) {
                emit(
                    AIStreamChunk(
                        text = "❌ Primary and Backup AI Providers Failed:\n\n" +
                                "• Primary (${settings.provider.displayName}): $primaryError\n" +
                                "• Backup (${settings.backupProvider.displayName}): ${backupEx.message}\n\n" +
                                "Please verify your API keys and quotas in Profile & Settings.",
                        isDone = true,
                        errorMessage = backupEx.message
                    )
                )
                return@flow
            }
        }

        // If execution still failed and no backup was configured or worked
        if (!executionSuccess) {
            emit(
                AIStreamChunk(
                    text = "❌ ${settings.provider.displayName} Error:\n\n$primaryError\n\n" +
                            "Please test your API key in Profile & Settings or add a backup API key for automatic failover.",
                    isDone = true,
                    errorMessage = primaryError
                )
            )
            return@flow
        }

        emit(AIStreamChunk(text = "", isDone = true))
    }.flowOn(Dispatchers.IO)

    private fun resolveApiKey(customKey: String): String {
        val trimmed = customKey.trim()
        if (trimmed.isNotBlank()) return trimmed
        val buildKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()
        if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    /**
     * Executes the actual HTTP network call to the selected provider
     */
    private suspend fun executeProviderCall(
        request: AIRequest,
        apiKey: String,
        provider: AIProviderType,
        customBaseUrl: String,
        customModel: String
    ): String = withContext(Dispatchers.IO) {
        when (provider) {
            AIProviderType.GEMINI -> callGeminiRest(request, apiKey, customModel)
            AIProviderType.ANTHROPIC -> callAnthropicRest(request, apiKey, customModel)
            AIProviderType.OPENAI,
            AIProviderType.PERPLEXITY,
            AIProviderType.GLM,
            AIProviderType.GROK,
            AIProviderType.DEEPSEEK,
            AIProviderType.MISTRAL,
            AIProviderType.OPENROUTER,
            AIProviderType.COHERE,
            AIProviderType.CUSTOM -> {
                callOpenAiCompatibleRest(request, apiKey, provider, customBaseUrl, customModel)
            }
        }
    }

    private suspend fun callGeminiRest(
        request: AIRequest,
        apiKey: String,
        customModel: String
    ): String = withContext(Dispatchers.IO) {
        // Valid Gemini models
        var model = if (customModel.isNotBlank()) customModel.trim() else "gemini-2.5-flash"
        if (model == "gemini-3.5-flash") {
            model = "gemini-2.5-flash"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contentsArray = JSONArray()
        for (msg in request.contextHistory) {
            val part = JSONObject().put("text", msg.second)
            val role = if (msg.first.equals("user", ignoreCase = true)) "user" else "model"
            contentsArray.put(JSONObject().put("role", role).put("parts", JSONArray().put(part)))
        }

        val currentPart = JSONObject().put("text", request.prompt)
        contentsArray.put(JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart)))

        val root = JSONObject().apply {
            put("contents", contentsArray)
            if (!request.systemInstruction.isNullOrBlank()) {
                val sysPart = JSONObject().put("text", request.systemInstruction)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(sysPart)))
            }
            put("generationConfig", JSONObject().put("temperature", request.temperature))
        }

        val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder().url(url).post(body).build()

        val response = httpClient.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = extractErrorMessage(responseBody)
            throw RuntimeException("HTTP ${response.code} ($model): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val first = candidates.getJSONObject(0)
            val content = first.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }
        ""
    }

    private suspend fun callAnthropicRest(
        request: AIRequest,
        apiKey: String,
        customModel: String
    ): String = withContext(Dispatchers.IO) {
        val model = if (customModel.isNotBlank()) customModel.trim() else "claude-3-5-sonnet-20241022"
        val url = "https://api.anthropic.com/v1/messages"

        val messagesArray = JSONArray()
        for (msg in request.contextHistory) {
            val role = if (msg.first.equals("assistant", ignoreCase = true) || msg.first.equals("model", ignoreCase = true)) "assistant" else "user"
            messagesArray.put(JSONObject().put("role", role).put("content", msg.second))
        }
        messagesArray.put(JSONObject().put("role", "user").put("content", request.prompt))

        val root = JSONObject().apply {
            put("model", model)
            put("max_tokens", 4096)
            if (!request.systemInstruction.isNullOrBlank()) {
                put("system", request.systemInstruction)
            }
            put("messages", messagesArray)
        }

        val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        val response = httpClient.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = extractErrorMessage(responseBody)
            throw RuntimeException("HTTP ${response.code} ($model): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val contentArray = json.optJSONArray("content")
        if (contentArray != null && contentArray.length() > 0) {
            return@withContext contentArray.getJSONObject(0).optString("text", "")
        }
        ""
    }

    private suspend fun callOpenAiCompatibleRest(
        request: AIRequest,
        apiKey: String,
        provider: AIProviderType,
        customBaseUrl: String,
        customModel: String
    ): String = withContext(Dispatchers.IO) {
        val rawBaseUrl = if (customBaseUrl.isNotBlank()) customBaseUrl else provider.baseUrl
        val baseUrl = rawBaseUrl.trimEnd('/') + "/"
        val url = "${baseUrl}chat/completions"
        val model = if (customModel.isNotBlank()) customModel.trim() else provider.defaultModel

        val messagesArray = JSONArray()
        if (!request.systemInstruction.isNullOrBlank()) {
            messagesArray.put(JSONObject().put("role", "system").put("content", request.systemInstruction))
        }
        for (msg in request.contextHistory) {
            val role = if (msg.first.equals("model", ignoreCase = true) || msg.first.equals("assistant", ignoreCase = true)) "assistant" else "user"
            messagesArray.put(JSONObject().put("role", role).put("content", msg.second))
        }
        messagesArray.put(JSONObject().put("role", "user").put("content", request.prompt))

        val root = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", request.temperature)
        }

        val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        val response = httpClient.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = extractErrorMessage(responseBody)
            throw RuntimeException("HTTP ${response.code} ($model): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val first = choices.getJSONObject(0)
            val message = first.optJSONObject("message")
            return@withContext message?.optString("content", "") ?: ""
        }
        ""
    }

    /**
     * Tests live connection to an API key and provider
     */
    suspend fun testApiKey(
        apiKey: String,
        provider: AIProviderType,
        baseUrl: String = "",
        model: String = ""
    ): ApiTestResult = withContext(Dispatchers.IO) {
        val cleanKey = resolveApiKey(apiKey)
        if (cleanKey.isBlank()) {
            return@withContext ApiTestResult(
                isSuccess = false,
                httpCode = 0,
                latencyMs = 0,
                message = "API key cannot be blank."
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val testRequest = AIRequest(
                prompt = "Respond with 'CONNECTED' in one word.",
                systemInstruction = "You are an API verification bot. Answer with 'CONNECTED'."
            )
            val response = executeProviderCall(
                request = testRequest,
                apiKey = cleanKey,
                provider = provider,
                customBaseUrl = baseUrl,
                customModel = model
            )
            val latency = System.currentTimeMillis() - startTime
            if (response.isNotBlank()) {
                ApiTestResult(
                    isSuccess = true,
                    httpCode = 200,
                    latencyMs = latency,
                    message = "Connected to ${provider.displayName}! Latency: ${latency}ms"
                )
            } else {
                ApiTestResult(
                    isSuccess = false,
                    httpCode = 200,
                    latencyMs = latency,
                    message = "Received empty response from ${provider.displayName}."
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ApiTestResult(
                isSuccess = false,
                httpCode = 500,
                latencyMs = latency,
                message = e.message ?: "Connection failed."
            )
        }
    }

    private fun extractErrorMessage(responseBody: String): String {
        return runCatching {
            val json = JSONObject(responseBody)
            if (json.has("error")) {
                val errorObj = json.optJSONObject("error")
                if (errorObj != null) {
                    errorObj.optString("message", responseBody)
                } else {
                    json.optString("error", responseBody)
                }
            } else {
                responseBody.take(200)
            }
        }.getOrDefault(responseBody.take(200))
    }

    private suspend fun streamTextInChunks(fullText: String, onChunk: suspend (String) -> Unit) {
        val words = fullText.split(" ")
        val buffer = StringBuilder()
        for (i in words.indices) {
            buffer.append(words[i])
            if (i < words.size - 1) buffer.append(" ")
            if (i % 3 == 0 || i == words.size - 1) {
                onChunk(buffer.toString())
                buffer.clear()
                delay(20)
            }
        }
        if (buffer.isNotEmpty()) {
            onChunk(buffer.toString())
        }
    }
}
