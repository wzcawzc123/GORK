package fuck.andes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val selectedProviderId: String? = null,
    val selectedModelId: String? = null,
    val memoryEnabled: Boolean = true,
    // ── 本地四层记忆（L0–L3）──
    val fourLayerMemoryEnabled: Boolean = true,
    val memoryAutoDistillEnabled: Boolean = true,
    val memoryDistillCursor: Long = 0L,
)
