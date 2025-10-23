package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.UserDefinedDouble
import integration.core.UserDefinedInt
import integration.core.booleanData
import integration.core.doubleData
import integration.core.enumData
import integration.core.enumclass.Direction
import integration.core.intData
import integration.core.longData
import integration.core.offsetDateTimeData
import integration.core.stringData
import integration.core.userDefinedDoubleData
import integration.core.userDefinedIntData
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.nullLiteral
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
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
        assertEquals("The value must not contain the single quotation.", ex.message)
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
