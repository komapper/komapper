package integration.jdbc.postgresql

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

data class Json(val data: String)

@KomapperEntity
@KomapperTable("json_data")
data class JsonData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Json)
