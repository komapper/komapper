package integration.r2dbc

import integration.core.BigDecimalData
import integration.core.BigIntegerData
import integration.core.BooleanData
import integration.core.ByteArrayData
import integration.core.ByteData
import integration.core.ColorInfo
import integration.core.Dbms
import integration.core.DirectionInfo
import integration.core.DoubleData
import integration.core.EmbeddedEnumOrdinalData
import integration.core.EmbeddedEnumPropertyData
import integration.core.EnumData
import integration.core.EnumOrdinalData
import integration.core.EnumPropertyData
import integration.core.FloatData
import integration.core.InstantData
import integration.core.IntData
import integration.core.LocalDateData
import integration.core.LocalDateTimeData
import integration.core.LocalTimeData
import integration.core.LongData
import integration.core.OffsetDateTimeData
import integration.core.Run
import integration.core.ShortData
import integration.core.StringData
import integration.core.UByteData
import integration.core.UIntData
import integration.core.UIntVersion
import integration.core.UShortData
import integration.core.UUIDData
import integration.core.UnsignedAddress
import integration.core.UnsignedAddress2
import integration.core.UnsignedIdentityStrategy
import integration.core.UnsignedSequenceStrategy
import integration.core.UserDefinedInt
import integration.core.UserDefinedIntData
import integration.core.UserDefinedString
import integration.core.UserDefinedStringData
import integration.core.WrappedString
import integration.core.WrappedStringData
import integration.core.bigDecimalData
import integration.core.bigIntegerData
import integration.core.booleanData
import integration.core.byteArrayData
import integration.core.byteData
import integration.core.doubleData
import integration.core.embeddedEnumOrdinalData
import integration.core.embeddedEnumPropertyData
import integration.core.enumData
import integration.core.enumOrdinalData
import integration.core.enumPropertyData
import integration.core.enumclass.Color
import integration.core.enumclass.Direction
import integration.core.floatData
import integration.core.instantData
import integration.core.intData
import integration.core.localDateData
import integration.core.localDateTimeData
import integration.core.localTimeData
import integration.core.longData
import integration.core.offsetDateTimeData
import integration.core.shortData
import integration.core.stringData
import integration.core.uByteData
import integration.core.uIntData
import integration.core.uShortData
import integration.core.unsignedAddress
import integration.core.unsignedAddress2
import integration.core.unsignedIdentityStrategy
import integration.core.unsignedSequenceStrategy
import integration.core.userDefinedIntData
import integration.core.userDefinedStringData
import integration.core.uuidData
import integration.core.wrappedStringData
import io.r2dbc.spi.Blob
import io.r2dbc.spi.Clob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.awaitFirst
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.runner.EnumMappingException
import org.komapper.core.dsl.runner.PropertyMappingException
import org.komapper.core.dsl.runner.ValueExtractingException
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcDataTypeTest(val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun array(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayData
        val array = arrayOf("A", "B", "C")
        val data = ArrayData(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val value = data2.value!!
        assertEquals(3, value.size)
        assertEquals("A", value[0])
        assertEquals("B", value[1])
        assertEquals("C", value[2])
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun array_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayData
        val data = ArrayData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun arrayOfNullable(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayOfNullableData
        val array = arrayOf("A", null, "C")
        val data = ArrayOfNullableData(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val value = data2.value!!
        assertEquals(3, value.size)
        assertEquals("A", value[0])
        assertNull(value[1])
        assertEquals("C", value[2])
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun arrayOfNullable_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.arrayOfNullableData
        val data = ArrayOfNullableData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigDecimalData
        val data = BigDecimalData(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigDecimal_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigDecimalData
        val data = BigDecimalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigIntegerData
        val data = BigIntegerData(1, BigInteger.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.bigIntegerData
        val data = BigIntegerData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    // jasync-r2dbc-mysql does not support blob type
    @Run(unless = [Dbms.MYSQL])
    @Test
    fun blob(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.blobData
        val p = flowOf(ByteBuffer.wrap(byteArrayOf(1, 2, 3))).asPublisher()
        val data = BlobData(1, Blob.from(p))
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val buffer = data2.value!!.stream().awaitFirst()
        assertEquals(1, buffer[0])
        assertEquals(2, buffer[1])
        assertEquals(3, buffer[2])
    }

    @Test
    fun blob_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.blobData
        val data = BlobData(1, null)
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.booleanData
        val data = BooleanData(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.booleanData
        val data = BooleanData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteData
        val data = ByteData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteData
        val data = ByteData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteArrayData
        val data = ByteArrayData(1, byteArrayOf(10, 20, 30))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.id, data2.id)
        assertContentEquals(data.value, data2.value)
    }

    @Test
    fun byteArray_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.byteArrayData
        val data = ByteArrayData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    // jasync-r2dbc-mysql does not support blob type
    @Run(unless = [Dbms.MYSQL])
    @Test
    fun clob(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobData
        val p = flowOf(CharBuffer.wrap("abc")).asPublisher()
        val data = ClobData(1, Clob.from(p))
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val buffer = data2.value!!.stream().awaitFirst()
        assertEquals('a', buffer[0])
        assertEquals('b', buffer[1])
        assertEquals('c', buffer[2])
    }

    @Test
    fun clob_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobData
        val data = ClobData(1, null)
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun clobString(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobStringData
        val data = ClobStringData(1, "abc")
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun clobString_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.clobStringData
        val data = ClobStringData(1, null)
        db.runQuery {
            QueryDsl.insert(m).single(data)
        }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.doubleData
        val data = DoubleData(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.doubleData
        val data = DoubleData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumData
        val data = EnumData(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumData
        val data = EnumData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_mapping_error(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumData
        db.runQuery { QueryDsl.executeScript("insert into enum_data (id, \"value\") values (1, 'unknown')") }
        val ex = assertFailsWith<PropertyMappingException> {
            db.runQuery {
                QueryDsl.from(m).where { m.id eq 1 }.first()
            }
            Unit
        }
        assertEquals("Failed to map a value to the property \"value\" of the entity class \"integration.core.EnumData\".", ex.message)
        val cause = ex.cause
        assertTrue(cause is ValueExtractingException)
        assertEquals("Failed to extract a value from column. The column index is 1.", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals("Failed to map the value \"unknown\" to the property \"name\" of the enum class \"integration.core.enumclass.Direction\".", cause2.message)
    }

    @Test
    fun enum_ordinal(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumOrdinalData
        val data = EnumOrdinalData(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_ordinal_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumOrdinalData
        val data = EnumOrdinalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_ordinal_mapping_error(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumOrdinalData
        db.runQuery { QueryDsl.executeScript("insert into enum_ordinal_data (id, \"value\") values (1, 90)") }
        val ex = assertFailsWith<PropertyMappingException> {
            db.runQuery {
                QueryDsl.from(m).where { m.id eq 1 }.first()
            }
            Unit
        }
        assertEquals("Failed to map a value to the property \"value\" of the entity class \"integration.core.EnumOrdinalData\".", ex.message)
        val cause = ex.cause
        assertTrue(cause is ValueExtractingException)
        assertEquals("Failed to extract a value from column. The column index is 1.", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals("Failed to map the value \"90\" to the property \"ordinal\" of the enum class \"integration.core.enumclass.Direction\".", cause2.message)
    }

    @Test
    fun enum_property(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumPropertyData
        val data = EnumPropertyData(1, Color.BLUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum_property_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumPropertyData
        val data = EnumPropertyData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    @Run(onlyIf = [Dbms.H2])
    fun enum_property_mapping_error(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.enumPropertyData
        db.runQuery { QueryDsl.executeScript("insert into enum_property_data (id, \"value\") values (1, 90)") }
        val ex = assertFailsWith<PropertyMappingException> {
            db.runQuery {
                QueryDsl.from(m).where { m.id eq 1 }.first()
            }
            Unit
        }
        assertEquals("Failed to map a value to the property \"value\" of the entity class \"integration.core.EnumPropertyData\".", ex.message)
        val cause = ex.cause
        assertTrue(cause is ValueExtractingException)
        assertEquals("Failed to extract a value from column. The column index is 1.", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals("Failed to map the value \"90\" to the property \"value\" of the enum class \"integration.core.enumclass.Color\".", cause2.message)
    }

    @Test
    fun embedded_enum_ordinal(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.embeddedEnumOrdinalData
        val data = EmbeddedEnumOrdinalData(1, DirectionInfo(Direction.EAST))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun embedded_enum_ordinal_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.embeddedEnumOrdinalData
        val data = EmbeddedEnumOrdinalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun embedded_enum_property(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.embeddedEnumPropertyData
        val data = EmbeddedEnumPropertyData(1, ColorInfo(Color.BLUE))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun embedded_enum_property_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.embeddedEnumPropertyData
        val data = EmbeddedEnumPropertyData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.floatData
        val data = FloatData(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.floatData
        val data = FloatData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun instant(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.instantData
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val value = dateTime.toInstant(ZoneOffset.UTC)
        val data = InstantData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    @Run(unless = [Dbms.MARIADB])
    fun instant_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.instantData
        val data = InstantData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intData
        val data = IntData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun int_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intData
        val data = IntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTimeData
        val data = LocalDateTimeData(
            1,
            LocalDateTime.of(2019, 6, 1, 12, 11, 10),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateTimeData
        val data = LocalDateTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateData
        val data = LocalDateData(
            1,
            LocalDate.of(2019, 6, 1),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localDateData
        val data = LocalDateData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    // TODO: jasync-r2dbc-mysql returns a Duration object for the LocalTime type
    @Run(unless = [Dbms.MYSQL])
    @Test
    fun localTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localTimeData
        val data = LocalTimeData(1, LocalTime.of(12, 11, 10))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.localTimeData
        val data = LocalTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.longData
        val data = LongData(1, 10L)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.longData
        val data = LongData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun offsetDateTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeData
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun offsetDateTime_postgresql(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeData
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val expected = OffsetDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
        assertEquals(expected, data2.value)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun offsetDateTime_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.offsetDateTimeData
        val data = OffsetDateTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.shortData
        val data = ShortData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.shortData
        val data = ShortData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun string(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.stringData
        val data = StringData(1, "ABC")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun string_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.stringData
        val data = StringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteData
        val data = UByteData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteData
        val data = UByteData(1, UByte.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uByteData
        val data = UByteData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntData
        val data = UIntData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntData
        val data = UIntData(1, UInt.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uIntData
        val data = UIntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortData
        val data = UShortData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsigned_autoIncrement(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedIdentityStrategy
        val data = UnsignedIdentityStrategy(null, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun unsigned_sequence(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedSequenceStrategy
        val data = UnsignedSequenceStrategy(0u, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedAddress
        val data = UnsignedAddress(16u, "STREET 16", 0u)
        val data2 = db.runQuery { QueryDsl.insert(m).single(data) }
        db.runQuery {
            QueryDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.addressId eq 16u }.first()
        }
        assertEquals(16u, data3.addressId)
        assertEquals(1u, data3.version)
    }

    @Test
    fun unsigned_version_valueClass(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.unsignedAddress2
        val data = UnsignedAddress2(16u, "STREET 16", null)
        val data2 = db.runQuery { QueryDsl.insert(m).single(data) }
        db.runQuery {
            QueryDsl.update(m).single(data2.copy(street = "STREET 16 B"))
        }
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.addressId eq 16u }.first()
        }
        assertEquals(16u, data3.addressId)
        assertEquals(UIntVersion(1u), data3.version)
    }

    @Test
    fun unsignedShort_maxValue(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortData
        val data = UShortData(1, UShort.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uShortData
        val data = UShortData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uuidData
        val value = UUID.randomUUID()
        val data = UUIDData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.uuidData
        val data = UUIDData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedInt(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.userDefinedIntData
        val data = UserDefinedIntData(1, UserDefinedInt(123))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedInt_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.userDefinedIntData
        val data = UserDefinedIntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedString(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.userDefinedStringData
        val data = UserDefinedStringData(1, UserDefinedString("ABC"))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedString_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.userDefinedStringData
        val data = UserDefinedStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun wrapperString(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.wrappedStringData
        val data = WrappedStringData(1, WrappedString("ABC"))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun wrappedString_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.wrappedStringData
        val data = WrappedStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
