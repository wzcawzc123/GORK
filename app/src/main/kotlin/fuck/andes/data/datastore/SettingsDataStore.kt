package fuck.andes.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fuck.andes.data.model.Settings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal object SettingsDataStore {
    private const val STORE_NAME = "fuck_andes_settings"

    private val SELECTED_PROVIDER_ID = stringPreferencesKey("selected_provider_id")
    private val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
    private val MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
    private val FOUR_LAYER_MEMORY_ENABLED = booleanPreferencesKey("four_layer_memory_enabled")
    private val MEMORY_AUTO_DISTILL_ENABLED = booleanPreferencesKey("memory_auto_distill_enabled")
    private val MEMORY_DISTILL_CURSOR = longPreferencesKey("memory_distill_cursor")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val THEME_ACCENT = stringPreferencesKey("theme_accent")
    private const val SELECTED_MODEL_BY_PROVIDER_PREFIX = "selected_model_id_by_provider."

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

    @Volatile
    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        if (!::dataStore.isInitialized) {
            dataStore = context.applicationContext.dataStore
        }
    }

    fun settingsFlow(): Flow<Settings> {
        ensureInitialized()
        return dataStore.data
            .catch { cause ->
                if (cause is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw cause
                }
            }
            .map { prefs ->
                Settings(
                    selectedProviderId = prefs[SELECTED_PROVIDER_ID],
                    selectedModelId = prefs[SELECTED_MODEL_ID],
                    memoryEnabled = prefs[MEMORY_ENABLED] ?: true,
                    fourLayerMemoryEnabled = prefs[FOUR_LAYER_MEMORY_ENABLED] ?: true,
                    memoryAutoDistillEnabled = prefs[MEMORY_AUTO_DISTILL_ENABLED] ?: true,
                    memoryDistillCursor = prefs[MEMORY_DISTILL_CURSOR] ?: 0L,
                    themeMode = prefs[THEME_MODE] ?: "system",
                    themeAccent = prefs[THEME_ACCENT],
                )
            }
    }

    suspend fun settings(): Settings = settingsFlow().first()

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        ensureInitialized()
        dataStore.edit { prefs ->
            val current = Settings(
                selectedProviderId = prefs[SELECTED_PROVIDER_ID],
                selectedModelId = prefs[SELECTED_MODEL_ID],
                memoryEnabled = prefs[MEMORY_ENABLED] ?: true,
                fourLayerMemoryEnabled = prefs[FOUR_LAYER_MEMORY_ENABLED] ?: true,
                memoryAutoDistillEnabled = prefs[MEMORY_AUTO_DISTILL_ENABLED] ?: true,
                memoryDistillCursor = prefs[MEMORY_DISTILL_CURSOR] ?: 0L,
                themeMode = prefs[THEME_MODE] ?: "system",
                themeAccent = prefs[THEME_ACCENT],
            )
            val updated = transform(current)
            prefs.putOrRemove(SELECTED_PROVIDER_ID, updated.selectedProviderId)
            prefs.putOrRemove(SELECTED_MODEL_ID, updated.selectedModelId)
            prefs[MEMORY_ENABLED] = updated.memoryEnabled
            prefs[FOUR_LAYER_MEMORY_ENABLED] = updated.fourLayerMemoryEnabled
            prefs[MEMORY_AUTO_DISTILL_ENABLED] = updated.memoryAutoDistillEnabled
            prefs[MEMORY_DISTILL_CURSOR] = updated.memoryDistillCursor
            prefs[THEME_MODE] = updated.themeMode
            prefs.putOrRemove(THEME_ACCENT, updated.themeAccent)
        }
    }

    fun selectedProviderIdFlow(): Flow<String?> =
        settingsFlow().map { it.selectedProviderId }

    fun selectedModelIdFlow(): Flow<String?> =
        settingsFlow().map { it.selectedModelId }

    suspend fun selectedModelIdForProvider(providerId: String): String? {
        ensureInitialized()
        return dataStore.data
            .catch { cause ->
                if (cause is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw cause
                }
            }
            .map { prefs -> prefs[selectedModelByProviderKey(providerId)] }
            .first()
    }

    fun memoryEnabledFlow(): Flow<Boolean> =
        settingsFlow().map { it.memoryEnabled }

    fun themeModeFlow(): Flow<String> =
        settingsFlow().map { it.themeMode }

    suspend fun setThemeMode(mode: String) {
        updateSettings { it.copy(themeMode = mode) }
    }

    fun accentColorNameFlow(): Flow<String?> =
        settingsFlow().map { it.themeAccent }

    suspend fun setAccentColorName(name: String?) {
        updateSettings { it.copy(themeAccent = name) }
    }

    suspend fun setSelectedProviderId(id: String?) {
        updateSettings { it.copy(selectedProviderId = id) }
    }

    suspend fun setSelectedModelId(id: String?) {
        updateSettings { it.copy(selectedModelId = id) }
    }

    suspend fun setSelection(providerId: String?, modelId: String?) {
        ensureInitialized()
        dataStore.edit { prefs ->
            val previousProviderId = prefs[SELECTED_PROVIDER_ID]
            val previousModelId = prefs[SELECTED_MODEL_ID]
            if (previousProviderId != null && previousModelId != null) {
                prefs[selectedModelByProviderKey(previousProviderId)] = previousModelId
            }

            prefs.putOrRemove(SELECTED_PROVIDER_ID, providerId)
            prefs.putOrRemove(SELECTED_MODEL_ID, modelId)
            if (providerId != null && modelId != null) {
                prefs[selectedModelByProviderKey(providerId)] = modelId
            }
        }
    }

    suspend fun clearSelectedModelIdForProvider(providerId: String) {
        ensureInitialized()
        dataStore.edit { prefs ->
            prefs.remove(selectedModelByProviderKey(providerId))
        }
    }

    suspend fun setMemoryEnabled(enabled: Boolean) {
        updateSettings { it.copy(memoryEnabled = enabled) }
    }

    fun fourLayerMemoryEnabledFlow(): Flow<Boolean> =
        settingsFlow().map { it.fourLayerMemoryEnabled }

    suspend fun setFourLayerMemoryEnabled(enabled: Boolean) {
        updateSettings { it.copy(fourLayerMemoryEnabled = enabled) }
    }

    fun memoryAutoDistillEnabledFlow(): Flow<Boolean> =
        settingsFlow().map { it.memoryAutoDistillEnabled }

    suspend fun setMemoryAutoDistillEnabled(enabled: Boolean) {
        updateSettings { it.copy(memoryAutoDistillEnabled = enabled) }
    }

    /** 自动沉淀游标（已处理到的 L0 createdAt），进程内缓存 + DataStore 持久化。 */
    suspend fun setMemoryDistillCursor(value: Long) {
        updateSettings { it.copy(memoryDistillCursor = value) }
    }

    private fun ensureInitialized() {
        check(::dataStore.isInitialized) {
            "SettingsDataStore.init(context) must be called in Application.onCreate()"
        }
    }

    private fun selectedModelByProviderKey(providerId: String): Preferences.Key<String> =
        stringPreferencesKey("$SELECTED_MODEL_BY_PROVIDER_PREFIX$providerId")

    private fun MutablePreferences.putOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value.isNullOrBlank()) {
            remove(key)
        } else {
            this[key] = value
        }
    }
}
