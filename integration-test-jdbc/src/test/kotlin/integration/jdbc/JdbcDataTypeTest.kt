package integration.jdbc

import integration.core.AnyData
import integration.core.AnyPerson
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
import integration.core.EnumUdtData
import integration.core.FloatData
import integration.core.InstantData
import integration.core.IntData
import integration.core.LocalDateData
import integration.core.LocalDateTimeData
import integration.core.LocalTimeData
import integration.core.LongData
import integration.core.OffsetDateTimeData
import integration.core.PairOfIntData
import integration.core.PairOfStringData
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
import integration.core.anyData
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
import integration.core.enumUdtData
import integration.core.enumclass.Color
import integration.core.enumclass.Direction
import integration.core.enumclass.Mood
import integration.core.floatData
import integration.core.instantData
import integration.core.intData
import integration.core.localDateData
import integration.core.localDateTimeData
import integration.core.localTimeData
import integration.core.longData
import integration.core.offsetDateTimeData
import integration.core.pairOfIntData
import integration.core.pairOfStringData
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
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.runner.EnumMappingException
import org.komapper.core.dsl.runner.PropertyMappingException
import org.komapper.core.dsl.runner.ValueExtractingException
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import java.math.BigInteger
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcDataTypeTest(val db: JdbcDatabase) {
    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any() {
        val m = Meta.anyData
        val data = AnyData(
            1,
            AnyPerson("ABC"),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery(
            QueryDsl.from(m).where { m.value eq AnyPerson("ABC") }.first(),
        )
        assertEquals(data, data3)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun any_null() {
        val m = Meta.anyData
        val data = AnyData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun array() {
        val m = Meta.arrayData
        val array = db.dataFactory.createArrayOf("text", listOf("A", "B", "C"))
        val data = ArrayData(1, array)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val anyArray = data2.value!!.getArray(1, 3) as Array<*>
        assertEquals(3, anyArray.size)
        assertEquals("A", anyArray[0])
        assertEquals("B", anyArray[1])
        assertEquals("C", anyArray[2])
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
    fun array_null() {
        val m = Meta.arrayData
        val data = ArrayData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun bigDecimal() {
        val m = Meta.bigDecimalData
        val data = BigDecimalData(1, BigDecimal.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq BigDecimal.TEN }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun bigDecimal_null() {
        val m = Meta.bigDecimalData
        val data = BigDecimalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun bigInteger() {
        val m = Meta.bigIntegerData
        val data = BigIntegerData(1, BigInteger.TEN)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq BigInteger.TEN }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun bigInteger_null() {
        val m = Meta.bigIntegerData
        val data = BigIntegerData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun blob() {
        val m = Meta.blobData
        val blob = db.dataFactory.createBlob()
        blob.setBytes(1, byteArrayOf(1, 2, 3))
        val data = BlobData(1, blob)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val bytes = data2.value!!.getBytes(1, 3)
        assertEquals(1, bytes[0])
        assertEquals(2, bytes[1])
        assertEquals(3, bytes[2])
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun blob_null() {
        val m = Meta.blobData
        val data = BlobData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun boolean() {
        val m = Meta.booleanData
        val data = BooleanData(1, true)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq true }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun boolean_null() {
        val m = Meta.booleanData
        val data = BooleanData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byte() {
        val m = Meta.byteData
        val data = ByteData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10 }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun byte_null() {
        val m = Meta.byteData
        val data = ByteData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun byteArray() {
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
    fun byteArray_null() {
        val m = Meta.byteArrayData
        val data = ByteArrayData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertContentEquals(data.value, data2.value)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun clob() {
        val m = Meta.clobData
        val clob = db.dataFactory.createClob()
        clob.setString(1, "abc")
        val data = ClobData(1, clob)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        val string = data2.value!!.getSubString(1, 3)
        assertEquals("abc", string)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun clob_null() {
        val m = Meta.clobData
        val data = ClobData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun clobString() {
        val m = Meta.clobStringData
        val data = ClobStringData(1, "abc")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun clobString_null() {
        val m = Meta.clobStringData
        val data = ClobStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun blobByteArray() {
        val m = Meta.blobByteArrayData
        val testBytes = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 16, 0, 0, 0, 16, 8, 6, 0, 0, 0, 31, -13, -1, 97, 0, 0, 1, 69, 73, 68, 65, 84, 120, -38, -107, -109, 75, 106, -125, 80, 20, -86, 45, 37, -84, -114, 66, -23, 34, 58, -24, -72, 3, -41, -40, 117, -108, -84, 34, -120, 66, 10, -50, -36, -127, 115, -33, -58, -73, 38, -98, -98, 95, -18, 9, 87, 47, 41, 116, -16, -93, 81, -65, -17, 60, 52, 22, 17, 61, 89, -106, -11, -58, -7, -8, 71, -34, 57, -5, -123, 85, 2, 59, 78, 18, -118, -30, -104, -110, 52, -91, 52, -53, 40, -53, 115, -54, -117, -126, -118, -78, -92, -14, 114, -95, -117, -92, -86, -88, -82, 107, 98, -26, 117, 37, -120, 98, 37, 96, -111, 8, 10, 22, -108, 44, 16, -80, 82, -87, -101, -58, 20, 0, 66, 117, -87, -68, -128, 0, -72, 26, -128, 6, 105, 91, 106, 57, 93, -39, -99, -126, -100, 65, -87, -118, -118, 0, 1, 9, -48, -9, -3, 61, -61, 48, -104, 2, -52, 90, -88, 118, 49, 99, -93, -127, 0, -58, 113, 92, -59, 16, -56, -110, 0, -93, -22, 22, -100, -90, -23, -98, -21, -11, 106, 10, 42, 53, -17, 22, -42, 33, -55, -19, 118, 51, 5, -78, 40, -76, -83, -61, 2, 72, -26, 121, 94, 98, 8, 100, -61, -113, 96, -100, -29, -70, -36, 55, 4, -82, 109, 60, -80, -123, -15, 27, -9, -15, -122, 50, 126, -35, 56, 50, 115, 88, 9, 100, -29, -37, -22, 56, 98, -76, 48, 12, -23, 124, -2, -95, 32, 8, -56, 113, -100, 47, 102, -98, 13, -127, -34, -66, 84, -57, 53, 124, 35, -98, -25, 45, -107, 79, -89, -45, -111, -97, -33, -127, 91, 9, -74, -101, 23, 1, 90, -113, -94, -120, 124, -33, 39, -41, 117, -65, 109, -37, 126, 17, 88, 23, 124, -10, 15, -106, -121, -50, -16, -1, 80, 109, -17, 116, 88, 23, -20, -79, -43, 63, 114, -112, -99, -73, -7, 5, -60, -23, -13, 112, 76, -55, -91, 117, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126)
        val data = BlobByteArrayData(1, testBytes)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.id, data2.id)
        assertContentEquals(data.value, data2.value)
    }

    @Test
    fun blobByteArray_null() {
        val m = Meta.blobByteArrayData
        val data = BlobByteArrayData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun double() {
        val m = Meta.doubleData
        val data = DoubleData(1, 10.0)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10.0 }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun double_null() {
        val m = Meta.doubleData
        val data = DoubleData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun enum() {
        val m = Meta.enumData
        val data = EnumData(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq Direction.EAST }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun enum_null() {
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
    fun enum_mapping_error() {
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
        assertEquals("Failed to extract a value from column. The column index is 1. (Column indices start from 0.)", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals(
            "Failed to map the value \"unknown\" to the property \"name\" of the enum class \"integration.core.enumclass.Direction\".",
            cause2.message
        )
    }

    @Test
    fun enum_ordinal() {
        val m = Meta.enumOrdinalData
        val data = EnumOrdinalData(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq Direction.EAST }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun enum_ordinal_null() {
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
    fun enum_ordinal_mapping_error() {
        val m = Meta.enumOrdinalData
        db.runQuery { QueryDsl.executeScript("insert into enum_ordinal_data (id, \"value\") values (1, 90)") }
        val ex = assertFailsWith<PropertyMappingException> {
            db.runQuery {
                QueryDsl.from(m).where { m.id eq 1 }.first()
            }
            Unit
        }
        assertEquals(
            "Failed to map a value to the property \"value\" of the entity class \"integration.core.EnumOrdinalData\".",
            ex.message
        )
        val cause = ex.cause
        assertTrue(cause is ValueExtractingException)
        assertEquals("Failed to extract a value from column. The column index is 1. (Column indices start from 0.)", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals(
            "Failed to map the value \"90\" to the property \"ordinal\" of the enum class \"integration.core.enumclass.Direction\".",
            cause2.message
        )
    }

    @Test
    fun enum_property() {
        val m = Meta.enumPropertyData
        val data = EnumPropertyData(1, Color.BLUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq Color.BLUE }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun enum_property_null() {
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
    fun enum_property_mapping_error() {
        val m = Meta.enumPropertyData
        db.runQuery { QueryDsl.executeScript("insert into enum_property_data (id, \"value\") values (1, 90)") }
        val ex = assertFailsWith<PropertyMappingException> {
            db.runQuery {
                QueryDsl.from(m).where { m.id eq 1 }.first()
            }
            Unit
        }
        assertEquals(
            "Failed to map a value to the property \"value\" of the entity class \"integration.core.EnumPropertyData\".",
            ex.message
        )
        val cause = ex.cause
        assertTrue(cause is ValueExtractingException)
        assertEquals("Failed to extract a value from column. The column index is 1. (Column indices start from 0.)", cause.message)
        val cause2 = cause.cause
        assertTrue(cause2 is EnumMappingException)
        assertEquals(
            "Failed to map the value \"90\" to the property \"value\" of the enum class \"integration.core.enumclass.Color\".",
            cause2.message
        )
    }

    @Test
    fun embedded_enum_ordinal() {
        val m = Meta.embeddedEnumOrdinalData
        val data = EmbeddedEnumOrdinalData(1, DirectionInfo(Direction.EAST))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq DirectionInfo(Direction.EAST) }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun embedded_enum_ordinal_null() {
        val m = Meta.embeddedEnumOrdinalData
        val data = EmbeddedEnumOrdinalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun embedded_enum_property() {
        val m = Meta.embeddedEnumPropertyData
        val data = EmbeddedEnumPropertyData(1, ColorInfo(Color.BLUE))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq ColorInfo(Color.BLUE) }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun embedded_enum_property_null() {
        val m = Meta.embeddedEnumPropertyData
        val data = EmbeddedEnumPropertyData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    @Run(onlyIf = [Dbms.POSTGRESQL])
    fun enum_udt() {
        val m = Meta.enumUdtData
        val data = EnumUdtData(1, Mood.HAPPY)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq Mood.HAPPY }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    @Run(onlyIf = [Dbms.POSTGRESQL])
    fun enum_udt_null() {
        val m = Meta.enumUdtData
        val data = EnumUdtData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun float() {
        val m = Meta.floatData
        val data = FloatData(1, 10.0f)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10.0f }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun float_null() {
        val m = Meta.floatData
        val data = FloatData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun instant() {
        val m = Meta.instantData
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val value = dateTime.toInstant(ZoneOffset.UTC)
        val data = InstantData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data3)
    }

    @Run(unless = [Dbms.MARIADB, Dbms.MYSQL_5])
    @Test
    fun instant_null() {
        val m = Meta.instantData
        val data = InstantData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.MARIADB])
    @Test
    fun instant_automatic() {
        val m = Meta.instantData
        val data = InstantData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNotNull(data2.value)
    }

    @Test
    fun int() {
        val m = Meta.intData
        val data = IntData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10 }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun int_null() {
        val m = Meta.intData
        val data = IntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = Meta.localDateTimeData
        val value = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val data = LocalDateTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun localDateTime_null() {
        val m = Meta.localDateTimeData
        val data = LocalDateTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate() {
        val m = Meta.localDateData
        val value = LocalDate.of(2019, 6, 1)
        val data = LocalDateData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun localDate_null() {
        val m = Meta.localDateData
        val data = LocalDateData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime() {
        val m = Meta.localTimeData
        val value = LocalTime.of(12, 11, 10)
        val data = LocalTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun localTime_findByValue() {
        val m = Meta.localTimeData
        val value = LocalTime.of(12, 11, 10)
        val data = LocalTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localTime_null() {
        val m = Meta.localTimeData
        val data = LocalTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun long() {
        val m = Meta.longData
        val data = LongData(1, 10L)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10L }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun long_null() {
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
    fun offsetDateTime() {
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
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data3)
    }

    @Run(onlyIf = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun offsetDateTime_mysql() {
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

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun offsetDateTime_postgresql() {
        val m = Meta.offsetDateTimeData
        val dateTime = LocalDateTime.of(2019, 6, 1, 12, 11, 10)
        val offset = ZoneOffset.ofHours(3)
        val value = OffsetDateTime.of(dateTime, offset)
        val data = OffsetDateTimeData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        println(data2)
        assertNotEquals(data, data2)
        assertEquals(value.toInstant(), data2.value!!.toInstant())
    }

    @Run(unless = [Dbms.MARIADB, Dbms.MYSQL_5])
    @Test
    fun offsetDateTime_null() {
        val m = Meta.offsetDateTimeData
        val data = OffsetDateTimeData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun pairOfInt() {
        val m = Meta.pairOfIntData
        val data = PairOfIntData(1, 10 to 20)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq (10 to 20) }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun pairOfInt_null() {
        val m = Meta.pairOfIntData
        val data = PairOfIntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun pairOfString() {
        val m = Meta.pairOfStringData
        val data = PairOfStringData(1, "a" to "b")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq ("a" to "b") }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun pairOfString_null() {
        val m = Meta.pairOfStringData
        val data = PairOfStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun short() {
        val m = Meta.shortData
        val data = ShortData(1, 10)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10 }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun short_null() {
        val m = Meta.shortData
        val data = ShortData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun sqlXml() {
        val m = Meta.sqlXmlData
        val value = db.dataFactory.createSQLXML()
        val xml = "<test>hello</test>"
        value.string = xml
        val data = SqlXmlData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(xml, data2.value!!.string)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun sqlXml_null() {
        val m = Meta.sqlXmlData
        val data = SqlXmlData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertNull(data2.value)
    }

    @Test
    fun string() {
        val m = Meta.stringData
        val data = StringData(1, "ABC")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq "ABC" }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun string_null() {
        val m = Meta.stringData
        val data = StringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte() {
        val m = Meta.uByteData
        val data = UByteData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10u }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun unsignedByte_maxValue() {
        val m = Meta.uByteData
        val data = UByteData(1, UByte.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedByte_null() {
        val m = Meta.uByteData
        val data = UByteData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt() {
        val m = Meta.uIntData
        val data = UIntData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10u }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun unsignedInt_maxValue() {
        val m = Meta.uIntData
        val data = UIntData(1, UInt.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedInt_null() {
        val m = Meta.uIntData
        val data = UIntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort() {
        val m = Meta.uShortData
        val data = UShortData(1, 10u)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq 10u }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun unsigned_autoIncrement() {
        val m = Meta.unsignedIdentityStrategy
        val data = UnsignedIdentityStrategy(null, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun unsigned_sequence() {
        val m = Meta.unsignedSequenceStrategy
        val data = UnsignedSequenceStrategy(0u, "test")
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1u }.first()
        }
        assertNotNull(data2)
    }

    @Test
    fun unsigned_version() {
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
    fun unsigned_version_valueClass() {
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
    fun unsignedShort_maxValue() {
        val m = Meta.uShortData
        val data = UShortData(1, UShort.MAX_VALUE)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun unsignedShort_null() {
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
    fun uuid() {
        val m = Meta.uuidData
        val value = UUID.randomUUID()
        val data = UUIDData(1, value)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq value }.first()
        }
        assertEquals(data, data3)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL, Dbms.H2])
    @Test
    fun uuid_null() {
        val m = Meta.uuidData
        val data = UUIDData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedInt() {
        val m = Meta.userDefinedIntData
        val data = UserDefinedIntData(1, UserDefinedInt(123))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq UserDefinedInt(123) }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun userDefinedInt_null() {
        val m = Meta.userDefinedIntData
        val data = UserDefinedIntData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun userDefinedString() {
        val m = Meta.userDefinedStringData
        val data = UserDefinedStringData(1, UserDefinedString("ABC"))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq UserDefinedString("ABC") }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun userDefinedString_null() {
        val m = Meta.userDefinedStringData
        val data = UserDefinedStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun wrapperString() {
        val m = Meta.wrappedStringData
        val data = WrappedStringData(1, WrappedString("ABC"))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
        val data3 = db.runQuery {
            QueryDsl.from(m).where { m.value eq WrappedString("ABC") }.first()
        }
        assertEquals(data, data3)
    }

    @Test
    fun wrappedString_null() {
        val m = Meta.wrappedStringData
        val data = WrappedStringData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
