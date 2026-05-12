import kotlinx.serialization.Serializable

@Serializable
data class Banner(
    val id: Int,
    val created_at: String,
    val title: String,
    val img_url: String
)