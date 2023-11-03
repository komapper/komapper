package integration.r2dbc.postgresql

import io.r2dbc.postgresql.codec.Box
import io.r2dbc.postgresql.codec.Circle
import io.r2dbc.postgresql.codec.Line
import io.r2dbc.postgresql.codec.Lseg
import io.r2dbc.postgresql.codec.Path
import io.r2dbc.postgresql.codec.Point
import io.r2dbc.postgresql.codec.Polygon
import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.locationtech.jts.geom.Geometry
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@KomapperEntity
@KomapperTable
data class R2dbcPostgreSqlMapping(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true)
    @Suppress("ArrayInDataClass")
    val array: Array<String>,
    @KomapperColumn(alwaysQuote = true) val bigDecimal: BigDecimal,
    @KomapperColumn(alwaysQuote = true) val bigInteger: BigInteger,
    @KomapperColumn(alwaysQuote = true) val blob: Blob,
    @KomapperColumn(alwaysQuote = true) val boolean: Boolean,
    @KomapperColumn(alwaysQuote = true) val box: Box,
    @KomapperColumn(alwaysQuote = true) val byte: Byte,
    @Suppress("ArrayInDataClass")
    @KomapperColumn(alwaysQuote = true) val byteArray: ByteArray,
    @KomapperColumn(alwaysQuote = true) val circle: Circle,
    @KomapperColumn(alwaysQuote = true) val clob: Clob,
    @KomapperColumn(alwaysQuote = true) val double: Double,
    @KomapperColumn(alwaysQuote = true) val float: Float,
    @KomapperColumn(alwaysQuote = true) val geometory: Geometry,
    @KomapperColumn(alwaysQuote = true) val instant: Instant,
    @KomapperColumn(alwaysQuote = true) val line: Line,
    @KomapperColumn(alwaysQuote = true) val lseg: Lseg,
    @KomapperColumn(alwaysQuote = true) val localDateTime: LocalDateTime,
    @KomapperColumn(alwaysQuote = true) val localDate: LocalDate,
    @KomapperColumn(alwaysQuote = true) val localTime: LocalTime,
    @KomapperColumn(alwaysQuote = true) val long: Long,
    @KomapperColumn(alwaysQuote = true) val offsetDateTime: OffsetDateTime,
    @KomapperColumn(alwaysQuote = true) val path: Path,
    @KomapperColumn(alwaysQuote = true) val point: Point,
    @KomapperColumn(alwaysQuote = true) val polygon: Polygon,
    @KomapperColumn(alwaysQuote = true) val short: Short,
    @KomapperColumn(alwaysQuote = true) val string: String,
    @KomapperColumn(alwaysQuote = true) val uByte: UByte,
    @KomapperColumn(alwaysQuote = true) val uInt: UInt,
    @KomapperColumn(alwaysQuote = true) val uShort: UShort,
    @KomapperColumn(alwaysQuote = true) val uuid: UUID,
)
