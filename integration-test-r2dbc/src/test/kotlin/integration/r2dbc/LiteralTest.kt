package integration.r2dbc

import integration.BooleanTest
import integration.IntTest
import integration.LongTest
import integration.StringTest
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(Env::class)
class LiteralTest(val db: R2dbcDatabase) {

    @Test
    fun test_boolean() = inTransaction(db) {
        val m = BooleanTest.meta
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id set 1
                m.value set literal(true)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(true)).first()
        }
        assertEquals(true to true, result)
    }

    @Test
    fun test_int() = inTransaction(db) {
        val m = IntTest.meta
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id set 1
                m.value set literal(123)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(345)).first()
        }
        assertEquals(123 to 345, result)
    }

    @Test
    fun test_long() = inTransaction(db) {
        val m = LongTest.meta
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id set 1
                m.value set literal(123L)
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal(345L)).first()
        }
        assertEquals(123L to 345L, result)
    }

    @Test
    fun test_string() = inTransaction(db) {
        val m = StringTest.meta
        db.runQuery {
            QueryDsl.insert(m).values {
                m.id set 1
                m.value set literal("hello")
            }
        }
        val result = db.runQuery {
            QueryDsl.from(m).select(m.value, literal("world")).first()
        }
        assertEquals("hello" to "world", result)
    }

    @Test
    fun test_illegal_string() = inTransaction(db) {
        val ex = assertFailsWith<IllegalArgumentException> {
            literal("I don't like it.")
        }
        assertEquals("The value must not contain the single quotation.", ex.message)
    }
}
