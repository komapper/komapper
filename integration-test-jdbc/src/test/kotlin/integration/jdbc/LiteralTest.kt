package integration.jdbc

import integration.BooleanTest
import integration.IntTest
import integration.LongTest
import integration.StringTest
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.jdbc.JdbcDatabase

@ExtendWith(Env::class)
class LiteralTest(val db: JdbcDatabase) {

    @Test
    fun test_boolean() {
        val m = BooleanTest.meta
        db.runQuery {
            SqlDsl.insert(m).values {
                m.id set 1
                m.value set literal(true)
            }
        }
        val result = db.runQuery {
            SqlDsl.from(m).select(m.value, literal(true)).first()
        }
        assertEquals(true to true, result)
    }

    @Test
    fun test_int() {
        val m = IntTest.meta
        db.runQuery {
            SqlDsl.insert(m).values {
                m.id set 1
                m.value set literal(123)
            }
        }
        val result = db.runQuery {
            SqlDsl.from(m).select(m.value, literal(345)).first()
        }
        assertEquals(123 to 345, result)
    }

    @Test
    fun test_long() {
        val m = LongTest.meta
        db.runQuery {
            SqlDsl.insert(m).values {
                m.id set 1
                m.value set literal(123L)
            }
        }
        val result = db.runQuery {
            SqlDsl.from(m).select(m.value, literal(345L)).first()
        }
        assertEquals(123L to 345L, result)
    }

    @Test
    fun test_string() {
        val m = StringTest.meta
        db.runQuery {
            SqlDsl.insert(m).values {
                m.id set 1
                m.value set literal("hello")
            }
        }
        val result = db.runQuery {
            SqlDsl.from(m).select(m.value, literal("world")).first()
        }
        assertEquals("hello" to "world", result)
    }

    @Test
    fun test_illegal_string() {
        val ex = assertThrows<IllegalArgumentException> {
            literal("I don't like it.")
        }
        assertEquals("The value must not contain the single quotation.", ex.message)
    }
}
