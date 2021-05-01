package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.literal
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class LiteralTest(val db: Database) {

    @Test
    fun test_boolean() {
        val m = BooleanTest.alias
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
        val m = IntTest.alias
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
        val m = LongTest.alias
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
        val m = StringTest.alias
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
