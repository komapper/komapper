package integration.r2dbc

import integration.core.booleanTest
import integration.core.intTest
import integration.core.longTest
import integration.core.stringTest
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(R2dbcEnv::class)
class R2dbcLiteralTest(val db: R2dbcDatabase) {

    @Test
    fun test_boolean() = inTransaction(db) {
        val m = Meta.booleanTest
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
    fun test_int() = inTransaction(db) {
        val m = Meta.intTest
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
    fun test_long() = inTransaction(db) {
        val m = Meta.longTest
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
    fun test_string() = inTransaction(db) {
        val m = Meta.stringTest
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
    fun test_illegal_string() = inTransaction(db) {
        val ex = assertFailsWith<IllegalArgumentException> {
            literal("I don't like it.")
        }
        assertEquals("The value must not contain the single quotation.", ex.message)
    }
}
