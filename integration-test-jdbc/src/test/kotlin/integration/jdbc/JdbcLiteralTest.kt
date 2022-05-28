package integration.jdbc

import integration.core.booleanData
import integration.core.intData
import integration.core.longData
import integration.core.stringData
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcLiteralTest(val db: JdbcDatabase) {

    @Test
    fun test_boolean() {
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
    fun test_int() {
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
    fun test_long() {
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
    fun test_string() {
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
    fun test_illegal_string() {
        val ex = assertFailsWith<IllegalArgumentException> {
            literal("I don't like it.")
        }
        assertEquals("The value must not contain the single quotation.", ex.message)
    }
}
