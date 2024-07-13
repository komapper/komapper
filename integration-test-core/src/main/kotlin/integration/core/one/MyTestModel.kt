package integration.core.one

import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
public data class MyTestModel(
    @KomapperId
    val id: Long,
)
