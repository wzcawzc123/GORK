package fuck.andes.data.model

data class Settings(
    val selectedProviderId: String? = null,
    val selectedModelId: String? = null,
    val memoryEnabled: Boolean = true,
    // ── 本地四层记忆（L0–L3）──
    val fourLayerMemoryEnabled: Boolean = true,
    val memoryAutoDistillEnabled: Boolean = true,
    val memoryDistillCursor: Long = 0L,
    // ── 外观与主题（上游 AppearanceSettings 架构）──
    val appearance: AppearanceSettings = AppearanceSettings(),
)
