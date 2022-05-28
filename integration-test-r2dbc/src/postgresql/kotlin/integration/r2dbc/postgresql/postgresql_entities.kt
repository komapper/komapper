package integration.r2dbc.postgresql

import io.r2dbc.postgresql.codec.Interval
import io.r2dbc.postgresql.codec.Json
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity
@KomapperTable("json_data")
data class JsonData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Json?)

@KomapperEntity
@KomapperTable("interval_data")
data class IntervalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Interval?)
