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

enum class AIProviderType(val displayName: String, val defaultModel: String, val baseUrl: String) {
    GEMINI("Google Gemini", "gemini-2.5-flash", "https://generativelanguage.googleapis.com/"),
    OPENAI("OpenAI", "gpt-4o", "https://api.openai.com/v1/"),
    ANTHROPIC("Anthropic Claude", "claude-3-5-sonnet-20241022", "https://api.anthropic.com/v1/"),
    PERPLEXITY("Perplexity AI", "sonar", "https://api.perplexity.ai/"),
    GLM("GLM (Zhipu AI)", "glm-4-flash", "https://open.bigmodel.cn/api/paas/v4/"),
    GROK("xAI Grok", "grok-2", "https://api.x.ai/v1/"),
    DEEPSEEK("DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1/"),
    MISTRAL("Mistral AI", "mistral-large-latest", "https://api.mistral.ai/v1/"),
    OPENROUTER("OpenRouter", "auto", "https://openrouter.ai/api/v1/"),
    COHERE("Cohere", "command-r-plus", "https://api.cohere.ai/v1/"),
    CUSTOM("Custom Model", "custom-model", "https://api.openai.com/v1/")
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
