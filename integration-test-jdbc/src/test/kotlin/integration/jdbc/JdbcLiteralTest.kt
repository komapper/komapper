package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.UserDefinedDouble
import integration.core.UserDefinedInt
import integration.core.booleanData
import integration.core.doubleData
import integration.core.enumData
import integration.core.enumOrdinalData
import integration.core.enumPropertyData
import integration.core.enumclass.Color
import integration.core.enumclass.Direction
import integration.core.intData
import integration.core.kotlinLocalDateData
import integration.core.kotlinLocalDateTimeData
import integration.core.localDateData
import integration.core.localDateTimeData
import integration.core.localTimeData
import integration.core.longData
import integration.core.offsetDateTimeData
import integration.core.stringData
import integration.core.userDefinedDoubleData
import integration.core.userDefinedIntData
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.nullLiteral
import org.komapper.core.dsl.query.first
import org.komapper.datetime.jdbc.literal
import org.komapper.jdbc.JdbcDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcLiteralTest(val db: JdbcDatabase) {
    @Test
    fun test_literal_boolean() {
        val m = Meta.booleanData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(true)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(true)).first()
        }
        assertEquals(true to true, result)
    }

    @Test
    fun test_literal_boolean_null() {
        val m = Meta.booleanData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as Boolean?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as Boolean?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_double() {
        val m = Meta.doubleData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(123.45)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(567.89)).first()
        }
        assertEquals(123.45 to 567.89, result)
    }

    @Test
    fun test_literal_double_null() {
        val m = Meta.doubleData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as Double?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as Double?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_int() {
        val m = Meta.intData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(123)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(345)).first()
        }
        assertEquals(123 to 345, result)
    }

    @Test
    fun test_literal_int_null() {
        val m = Meta.intData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as Int?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as Int?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_kotlinLocalDate() {
        val m = Meta.kotlinLocalDateData
        val date1 = LocalDate.of(2020, 12, 31).toKotlinLocalDate()
        val date2 = LocalDate.of(2021, 6, 15).toKotlinLocalDate()
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(date1)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(date2)).first()
        }
        assertEquals(date1 to date2, result)
    }

    @Test
    fun test_literal_kotlinLocalDate_null() {
        val m = Meta.kotlinLocalDateData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as kotlinx.datetime.LocalDate?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as kotlinx.datetime.LocalDate?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_kotlinLocalDateTime() {
        val m = Meta.kotlinLocalDateTimeData
        val date1 = LocalDateTime.of(2020, 12, 31, 13, 30, 20).toKotlinLocalDateTime()
        val date2 = LocalDateTime.of(2021, 6, 15, 1, 2, 3).toKotlinLocalDateTime()
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(date1)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(date2)).first()
        }
        assertEquals(date1 to date2, result)
    }

    @Test
    fun test_literal_kotlinLocalDateTime_null() {
        val m = Meta.kotlinLocalDateTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as kotlinx.datetime.LocalDateTime?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as kotlinx.datetime.LocalDateTime?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_localDate() {
        val m = Meta.localDateData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(LocalDate.of(2020, 12, 31))
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(LocalDate.of(2021, 6, 15))).first()
        }
        assertEquals(LocalDate.of(2020, 12, 31) to LocalDate.of(2021, 6, 15), result)
    }

    @Test
    fun test_literal_localDate_null() {
        val m = Meta.localDateData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as LocalDate?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as LocalDate?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_localDateTime() {
        val m = Meta.localDateTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(LocalDateTime.of(2020, 12, 31, 14, 30, 20))
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(LocalDateTime.of(2021, 6, 15, 1, 2, 3))).first()
        }
        assertEquals(LocalDateTime.of(2020, 12, 31, 14, 30, 20) to LocalDateTime.of(2021, 6, 15, 1, 2, 3), result)
    }

    @Test
    fun test_literal_localDateTime_null() {
        val m = Meta.localDateTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as LocalDateTime?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as LocalDateTime?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_localTime() {
        val m = Meta.localTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(LocalTime.of(14, 30, 20))
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(LocalTime.of(1, 2, 3))).first()
        }
        assertEquals(LocalTime.of(14, 30, 20) to LocalTime.of(1, 2, 3), result)
    }

    @Test
    fun test_literal_localTime_null() {
        val m = Meta.localTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as LocalTime?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as LocalTime?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_long() {
        val m = Meta.longData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(123L)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(345L)).first()
        }
        assertEquals(123L to 345L, result)
    }

    @Test
    fun test_literal_long_null() {
        val m = Meta.longData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as Long?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as Long?)).first()
        }
        assertEquals(null to null, result)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB])
    @Test
    fun test_literal_offsetDateTime() {
        val value = OffsetDateTime.parse("2025-10-23T12:34:56+09:00")
        val value2 = OffsetDateTime.parse("2030-11-24T12:34:56+09:00")
        val m = Meta.offsetDateTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(value2)).first()
        }
        assertTrue(value.isEqual(result.first))
        assertTrue(value2.isEqual(result.second))
    }

    @Run(onlyIf = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun test_literal_offsetDateTime_unsupportedException() {
        val value = OffsetDateTime.parse("2025-10-23T12:34:56+09:00")
        val m = Meta.offsetDateTimeData
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.insert(m).values {
                    m.id eq 1
                    m.value eq literal(value)
                }
            }
            Unit
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.MARIADB])
    @Test
    fun test_literal_offsetDateTime_illegalStateException() {
        val value = OffsetDateTime.parse("2025-10-23T12:34:56+09:00")
        val m = Meta.offsetDateTimeData
        val ex = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.insert(m).values {
                    m.id eq 1
                    m.value eq literal(value)
                }
            }
            Unit
        }
        println(ex)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB])
    @Test
    fun test_literal_offsetDateTime_null() {
        val m = Meta.offsetDateTimeData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as OffsetDateTime?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as OffsetDateTime?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_string() {
        val m = Meta.stringData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal("hello")
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal("world")).first()
        }
        assertEquals("hello" to "world", result)
    }

    @Test
    fun test_literal_string_null() {
        val m = Meta.stringData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null as String?)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null as String?)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_illegal_string() {
        val ex = assertFailsWith<IllegalArgumentException> {
            literal("I don't like it.")
        }
        assertEquals("The value must not contain the single quote.", ex.message)
    }

    @Test
    fun test_literal_enum() {
        val m = Meta.enumData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(Direction.EAST, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(Direction.WEST, m.value)).first()
        }
        assertEquals(Direction.EAST to Direction.WEST, result)
    }

    @Test
    fun test_literal_enum_null() {
        val m = Meta.enumData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null, m.value)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_enum_ordinal() {
        val m = Meta.enumOrdinalData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(Direction.EAST, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(Direction.WEST, m.value)).first()
        }
        assertEquals(Direction.EAST to Direction.WEST, result)
    }

    @Test
    fun test_literal_enum_ordinal_null() {
        val m = Meta.enumOrdinalData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null, m.value)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_enum_property() {
        val m = Meta.enumPropertyData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(Color.RED, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(Color.BLUE, m.value)).first()
        }
        assertEquals(Color.RED to Color.BLUE, result)
    }

    @Test
    fun test_literal_enum_property_null() {
        val m = Meta.enumPropertyData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null, m.value)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_literal_userDefinedInt() {
        val m = Meta.userDefinedIntData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(UserDefinedInt(123), m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(UserDefinedInt(456), m.value)).first()
        }
        assertEquals(UserDefinedInt(123) to UserDefinedInt(456), result)
    }

    @Test
    fun test_literal_userDefinedInt_null() {
        val m = Meta.userDefinedIntData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null, m.value)).first()
        }
        assertEquals(null to null, result)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun test_literal_userDefinedDouble() {
        val m = Meta.userDefinedDoubleData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(UserDefinedDouble(123.45), m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(UserDefinedDouble(567.89), m.value)).first()
        }
        assertEquals(UserDefinedDouble(123.45) to UserDefinedDouble(567.89), result)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun test_literal_userDefinedDouble_null() {
        val m = Meta.userDefinedDoubleData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq literal(null, m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(null, m.value)).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_nullLiteral_string_null() {
        val m = Meta.stringData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq nullLiteral(m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, nullLiteral()).first()
        }
        assertEquals(null to null, result)
    }

    @Test
    fun test_nullLiteral_enum_null() {
        val m = Meta.enumData
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id eq 1
                m.value eq nullLiteral(m.value)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, nullLiteral()).first()
        }
        assertEquals(null to null, result)
    }
}
