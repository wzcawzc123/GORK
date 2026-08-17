package fuck.andes.ui.app

import androidx.compose.ui.graphics.Color

/**
 * 主题强调色预设。
 *
 * 持久化只保存名称（见 SettingsDataStore 的 theme_accent），
 * ARGB 在此处统一映射，便于后续增删色板而不破坏既有配置。
 */
internal data class ThemeAccent(
    val name: String,
    val displayName: String,
    val argb: Long,
)

internal object ThemeAccentColors {
    val presets: List<ThemeAccent> = listOf(
        ThemeAccent("blue", "蓝", 0xFF0066FF),
        ThemeAccent("cyan", "青", 0xFF00A0A8),
        ThemeAccent("green", "绿", 0xFF34A853),
        ThemeAccent("orange", "橙", 0xFFFF7700),
        ThemeAccent("red", "红", 0xFFEB3B2F),
        ThemeAccent("purple", "紫", 0xFF9C5CFF),
        ThemeAccent("pink", "粉", 0xFFF25F9C),
        ThemeAccent("gold", "金", 0xFFF9AB00),
    )

    fun argbForName(name: String?): Long? =
        name?.let { presetName -> presets.firstOrNull { it.name == presetName }?.argb }

    fun nameForArgb(argb: Long?): String? =
        argb?.let { value -> presets.firstOrNull { it.argb == value }?.name }

    fun colorForName(name: String?): Color? = argbForName(name)?.let { Color(it) }
}
