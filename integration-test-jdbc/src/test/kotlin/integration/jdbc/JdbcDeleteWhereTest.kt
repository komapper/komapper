package integration.jdbc

import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcDeleteWhereTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }

    @Test
    fun allowMissingWhereClause_default() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).all()
            }
        }
        assertEquals("Missing where clause is not allowed.", ex.message)
    }

    @Test
    fun allowMissingWhereClause_default_empty() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).where { }
            }
        }
        assertEquals("Missing where clause is not allowed.", ex.message)
    }

    @Test
    fun allowMissingWhereClause_true() {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.delete(e).all().options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
