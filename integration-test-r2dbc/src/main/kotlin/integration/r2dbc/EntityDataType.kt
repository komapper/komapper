package integration.r2dbc

import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.core.type.ClobString

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_data")
data class ArrayData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<String>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_data")
data class ArrayOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<String?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_boolean_data")
data class ArrayBooleanData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Boolean>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_boolean_data")
data class ArrayBooleanOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Boolean?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_double_data")
data class ArrayDoubleData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Double>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_double_data")
data class ArrayDoubleOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Double?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_float_data")
data class ArrayFloatData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Float>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_float_data")
data class ArrayFloatOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Float?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_int_data")
data class ArrayIntData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Int>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_int_data")
data class ArrayIntOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Int?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_long_data")
data class ArrayLongData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Long>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_long_data")
data class ArrayLongOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Long?>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_short_data")
data class ArrayShortData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Short>?,
)

@Suppress("ArrayInDataClass")
@KomapperEntity
@KomapperTable("array_short_data")
data class ArrayShortOfNullableData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Array<Short?>?,
)

@KomapperEntity
@KomapperTable("blob_data")
data class BlobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Blob?,
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: Clob?,
)

@KomapperEntity
@KomapperTable("clob_data")
data class ClobStringData(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true, alternateType = ClobString::class)
    val value: String?,
)
