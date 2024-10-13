package integration.jdbc.postgresql

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import java.util.UUID

data class Json(val data: String)

@KomapperEntity
@KomapperTable("json_data")
data class JsonData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Json
)

@KomapperEntity
data class Friend(
    @KomapperId
    val uuid1: UUID,
    @KomapperId
    val uuid2: UUID,
    val pending: Boolean,
)
