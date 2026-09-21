package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeSetting(val displayName: String) {
    SYSTEM("Auto System"),
    LIGHT("Cream Minimal"),
    DARK("Charcoal Dark"),
    PASTEL_LAVENDER("Pastel Lavender"),
    PASTEL_MINT("Pastel Mint"),
    PASTEL_PEACH("Pastel Peach"),
    PASTEL_ROSE("Pastel Rose"),
    PASTEL_SKY("Pastel Sky")
}

enum class UserGender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female")
}

enum class AIProviderType(
    val displayName: String,
    val defaultModel: String,
    val baseUrl: String,
    val officialWebsite: String,
    val officialKeyUrl: String,
    val supportedModelsHint: String
) {
    GEMINI(
        displayName = "Google Gemini",
        defaultModel = "gemini-2.5-flash",
        baseUrl = "https://generativelanguage.googleapis.com/",
        officialWebsite = "https://aistudio.google.com",
        officialKeyUrl = "https://aistudio.google.com/app/apikey",
        supportedModelsHint = "gemini-2.5-flash, gemini-2.0-flash, gemini-1.5-pro"
    ),
    DEEPSEEK(
        displayName = "DeepSeek",
        defaultModel = "deepseek-chat",
        baseUrl = "https://api.deepseek.com/",
        officialWebsite = "https://www.deepseek.com",
        officialKeyUrl = "https://platform.deepseek.com/api_keys",
        supportedModelsHint = "deepseek-chat (V3), deepseek-reasoner (R1) • Note: 'deepseek-flash' does not exist"
    ),
    OPENAI(
        displayName = "OpenAI",
        defaultModel = "gpt-4o",
        baseUrl = "https://api.openai.com/v1/",
        officialWebsite = "https://openai.com",
        officialKeyUrl = "https://platform.openai.com/api-keys",
        supportedModelsHint = "gpt-4o, gpt-4o-mini, o1, o3-mini"
    ),
    ANTHROPIC(
        displayName = "Anthropic Claude",
        defaultModel = "claude-3-5-sonnet-20241022",
        baseUrl = "https://api.anthropic.com/v1/",
        officialWebsite = "https://anthropic.com",
        officialKeyUrl = "https://console.anthropic.com/settings/keys",
        supportedModelsHint = "claude-3-5-sonnet-20241022, claude-3-5-haiku"
    ),
    GROQ(
        displayName = "Groq",
        defaultModel = "llama-3.3-70b-versatile",
        baseUrl = "https://api.groq.com/openai/v1/",
        officialWebsite = "https://groq.com",
        officialKeyUrl = "https://console.groq.com/keys",
        supportedModelsHint = "llama-3.3-70b-versatile, deepseek-r1-distill-llama-70b"
    ),
    PERPLEXITY(
        displayName = "Perplexity AI",
        defaultModel = "sonar",
        baseUrl = "https://api.perplexity.ai/",
        officialWebsite = "https://www.perplexity.ai",
        officialKeyUrl = "https://www.perplexity.ai/settings/api",
        supportedModelsHint = "sonar, sonar-pro, sonar-reasoning"
    ),
    GLM(
        displayName = "GLM (Zhipu AI)",
        defaultModel = "glm-4-flash",
        baseUrl = "https://open.bigmodel.cn/api/paas/v4/",
        officialWebsite = "https://open.bigmodel.cn",
        officialKeyUrl = "https://open.bigmodel.cn/usercenter/apikeys",
        supportedModelsHint = "glm-4-flash, glm-4-plus"
    ),
    GROK(
        displayName = "xAI Grok",
        defaultModel = "grok-2",
        baseUrl = "https://api.x.ai/v1/",
        officialWebsite = "https://x.ai",
        officialKeyUrl = "https://console.x.ai/",
        supportedModelsHint = "grok-2, grok-beta"
    ),
    MISTRAL(
        displayName = "Mistral AI",
        defaultModel = "mistral-large-latest",
        baseUrl = "https://api.mistral.ai/v1/",
        officialWebsite = "https://mistral.ai",
        officialKeyUrl = "https://console.mistral.ai/api-keys/",
        supportedModelsHint = "mistral-large-latest, mistral-small-latest, codestral-latest"
    ),
    OPENROUTER(
        displayName = "OpenRouter",
        defaultModel = "auto",
        baseUrl = "https://openrouter.ai/api/v1/",
        officialWebsite = "https://openrouter.ai",
        officialKeyUrl = "https://openrouter.ai/keys",
        supportedModelsHint = "auto, deepseek/deepseek-r1, anthropic/claude-3.5-sonnet"
    ),
    COHERE(
        displayName = "Cohere",
        defaultModel = "command-r-plus",
        baseUrl = "https://api.cohere.ai/v1/",
        officialWebsite = "https://cohere.com",
        officialKeyUrl = "https://dashboard.cohere.com/api-keys",
        supportedModelsHint = "command-r-plus, command-r"
    ),
    CUSTOM(
        displayName = "Custom Model",
        defaultModel = "custom-model",
        baseUrl = "https://api.openai.com/v1/",
        officialWebsite = "https://platform.openai.com",
        officialKeyUrl = "https://platform.openai.com/api-keys",
        supportedModelsHint = "Any OpenAI-compatible endpoint & model"
    )
}

data class UserSettings(
    val userName: String = "Rahul",
    val userAge: String = "24",
    val userGender: UserGender = UserGender.MALE,
    val userPersonality: String = "Analytical, witty, visionary, high-agency and detail-oriented",
    val userAvatarUri: String = "",
    val theme: ThemeSetting = ThemeSetting.LIGHT,
    val isDarkMode: Boolean = false,
    val isCompactMode: Boolean = false,
    val isBiometricLockEnabled: Boolean = false,
    val provider: AIProviderType = AIProviderType.GEMINI,
    val customApiKey: String = "",
    val customBaseUrl: String = "",
    val customModel: String = "",
    val backupApiKey: String = "",
    val backupProvider: AIProviderType = AIProviderType.OPENAI,
    val isThinkingModeEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val systemInstruction: String = "You are MAX-N, an ultra-intelligent, precise, and sophisticated AI workspace assistant."
)

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("max_n_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val userName = prefs.getString("user_name", "Rahul") ?: "Rahul"
        val userAge = prefs.getString("user_age", "24") ?: "24"
        val genderStr = prefs.getString("user_gender", UserGender.MALE.name) ?: UserGender.MALE.name
        val userPersonality = prefs.getString("user_personality", "Analytical, witty, visionary, high-agency and detail-oriented")
            ?: "Analytical, witty, visionary, high-agency and detail-oriented"
        val userAvatarUri = prefs.getString("user_avatar_uri", "") ?: ""
        val themeStr = prefs.getString("theme", ThemeSetting.LIGHT.name) ?: ThemeSetting.LIGHT.name
        val isDarkMode = prefs.getBoolean("is_dark_mode", false)
        val isCompactMode = prefs.getBoolean("is_compact_mode", false)
        val isBiometricLockEnabled = prefs.getBoolean("is_biometric_lock", false)
        val providerStr = prefs.getString("provider", AIProviderType.GEMINI.name) ?: AIProviderType.GEMINI.name
        val customApiKey = prefs.getString("custom_api_key", "") ?: ""
        val customBaseUrl = prefs.getString("custom_base_url", "") ?: ""
        val customModel = prefs.getString("custom_model", "") ?: ""
        val backupApiKey = prefs.getString("backup_api_key", "") ?: ""
        val backupProviderStr = prefs.getString("backup_provider", AIProviderType.OPENAI.name) ?: AIProviderType.OPENAI.name
        val isThinking = prefs.getBoolean("thinking_mode", true)
        val hasCompletedOnboarding = prefs.getBoolean("completed_onboarding", false)
        val systemInstruction = prefs.getString(
            "system_instruction",
            "You are MAX-N, an ultra-intelligent, precise, and sophisticated AI workspace assistant."
        ) ?: "You are MAX-N, an ultra-intelligent, precise, and sophisticated AI workspace assistant."

        return UserSettings(
            userName = userName,
            userAge = userAge,
            userGender = runCatching { UserGender.valueOf(genderStr) }.getOrDefault(UserGender.MALE),
            userPersonality = userPersonality,
            userAvatarUri = userAvatarUri,
            theme = runCatching { ThemeSetting.valueOf(themeStr) }.getOrDefault(ThemeSetting.LIGHT),
            isDarkMode = isDarkMode,
            isCompactMode = isCompactMode,
            isBiometricLockEnabled = isBiometricLockEnabled,
            provider = runCatching { AIProviderType.valueOf(providerStr) }.getOrDefault(AIProviderType.GEMINI),
            customApiKey = customApiKey,
            customBaseUrl = customBaseUrl,
            customModel = customModel,
            backupApiKey = backupApiKey,
            backupProvider = runCatching { AIProviderType.valueOf(backupProviderStr) }.getOrDefault(AIProviderType.OPENAI),
            isThinkingModeEnabled = isThinking,
            hasCompletedOnboarding = hasCompletedOnboarding,
            systemInstruction = systemInstruction
        )
    }

    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _settings.value = _settings.value.copy(userName = name)
    }

    fun updateUserProfile(age: String, gender: UserGender, personality: String, avatarUri: String) {
        prefs.edit()
            .putString("user_age", age)
            .putString("user_gender", gender.name)
            .putString("user_personality", personality)
            .putString("user_avatar_uri", avatarUri)
            .apply()
        _settings.value = _settings.value.copy(
            userAge = age,
            userGender = gender,
            userPersonality = personality,
            userAvatarUri = avatarUri
        )
    }

    fun updateAvatarUri(uri: String) {
        prefs.edit().putString("user_avatar_uri", uri).apply()
        _settings.value = _settings.value.copy(userAvatarUri = uri)
    }

    fun updateTheme(theme: ThemeSetting) {
        prefs.edit().putString("theme", theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        _settings.value = _settings.value.copy(isDarkMode = enabled)
    }

    fun toggleCompactMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_compact_mode", enabled).apply()
        _settings.value = _settings.value.copy(isCompactMode = enabled)
    }

    fun toggleBiometricLock(enabled: Boolean) {
        prefs.edit().putBoolean("is_biometric_lock", enabled).apply()
        _settings.value = _settings.value.copy(isBiometricLockEnabled = enabled)
    }

    fun updateProvider(provider: AIProviderType) {
        prefs.edit().putString("provider", provider.name).apply()
        _settings.value = _settings.value.copy(provider = provider)
    }

    fun updateApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _settings.value = _settings.value.copy(customApiKey = key)
    }

    fun updateBackupApiKey(key: String) {
        prefs.edit().putString("backup_api_key", key).apply()
        _settings.value = _settings.value.copy(backupApiKey = key)
    }

    fun updateBackupProvider(provider: AIProviderType) {
        prefs.edit().putString("backup_provider", provider.name).apply()
        _settings.value = _settings.value.copy(backupProvider = provider)
    }

    fun updateBaseUrl(url: String) {
        prefs.edit().putString("custom_base_url", url).apply()
        _settings.value = _settings.value.copy(customBaseUrl = url)
    }

    fun updateModel(model: String) {
        prefs.edit().putString("custom_model", model).apply()
        _settings.value = _settings.value.copy(customModel = model)
    }

    fun toggleThinkingMode(enabled: Boolean) {
        prefs.edit().putBoolean("thinking_mode", enabled).apply()
        _settings.value = _settings.value.copy(isThinkingModeEnabled = enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("completed_onboarding", completed).apply()
        _settings.value = _settings.value.copy(hasCompletedOnboarding = completed)
    }

    fun updateSystemInstruction(instruction: String) {
        prefs.edit().putString("system_instruction", instruction).apply()
        _settings.value = _settings.value.copy(systemInstruction = instruction)
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _settings.value = loadSettings()
    }
}
