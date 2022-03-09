package integration.r2dbc

import integration.core.address
import integration.core.employee
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
    fun test() = inTransaction(db) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }

    @Test
    fun allowMissingWhereClause_default() = inTransaction(db) {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).all()
            }
        }
        println(ex)
    }

    @Test
    fun allowMissingWhereClause_true() = inTransaction(db) {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.delete(e).all().options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
