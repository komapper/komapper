package integration.r2dbc

import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(R2dbcEnv::class)
class R2dbcDeleteWhereTest(private val db: R2dbcDatabase) {

    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }

    @Test
    fun all(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.delete(e).all()
        }
        assertEquals(14, count)
    }

    @Test
    fun allowMissingWhereClause_default(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).where { }
            }
        }
        println(ex)
    }

    @Test
    fun allowMissingWhereClause_true(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.delete(e).where { }.options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
