package example

import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
data class Box(
    @KomapperId
    val id: Int,
    val name: String,
)
