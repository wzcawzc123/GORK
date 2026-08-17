package fuck.andes.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import fuck.andes.data.datastore.SettingsDataStore
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 全局主题：支持"跟随系统 / 浅色 / 深色"三种明暗模式与一组强调色。
 *
 * 主题配置保存在 SettingsDataStore（DataStore）中，设置页修改后这里实时响应；
 * MiuixTheme 与 MaterialTheme（markdown 渲染上下文）保持同一明暗状态。
 * 选择强调色时切换到 Monet 配色（以强调色为种子色），否则使用 Miuix 内置经典配色，
 * 保证不改变任何既有功能行为。
 */
@Composable
fun AgentAppTheme(content: @Composable () -> Unit) {
    val themeMode by SettingsDataStore.themeModeFlow().collectAsState(initial = "system")
    val accentName by SettingsDataStore.accentColorNameFlow().collectAsState(initial = null)

    val baseMode = when (themeMode) {
        "light" -> ColorSchemeMode.Light
        "dark" -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
    val accentArgb = ThemeAccentColors.argbForName(accentName)
    val colorSchemeMode = if (accentArgb != null) {
        when (baseMode) {
            ColorSchemeMode.Light -> ColorSchemeMode.MonetLight
            ColorSchemeMode.Dark -> ColorSchemeMode.MonetDark
            else -> ColorSchemeMode.MonetSystem
        }
    } else {
        baseMode
    }
    // ThemeController 的属性是只读 val 委托，无法原地改模式；明暗/强调色变化时按
    // colorSchemeMode + accentArgb 重建控制器（remember 键变化会替换实例并触发 Miuix 重组）。
    val controller = remember(colorSchemeMode, accentArgb) {
        ThemeController(
            colorSchemeMode = colorSchemeMode,
            keyColor = accentArgb?.let { Color(it) },
        )
    }
    MiuixTheme(controller = controller) {
        // MaterialTheme 仅向 markdown-renderer-m3 提供颜色/字体上下文，不用于 UI 组件
        val dark = colorSchemeMode == ColorSchemeMode.Dark ||
            colorSchemeMode == ColorSchemeMode.MonetDark ||
            ((colorSchemeMode == ColorSchemeMode.System ||
                colorSchemeMode == ColorSchemeMode.MonetSystem) && isSystemInDarkTheme())
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
            content = content,
        )
    }
}
