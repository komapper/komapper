package integration.r2dbc

import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertNull

@ExtendWith(R2dbcEnv::class)
class R2dbcDryRunTest(private val db: R2dbcDatabase) {
    @Test
    fun dryRunQuery() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 1 }
        val result = db.dryRunQuery(query)
        assertNull(result.throwable)
    }

    @Test
    fun dryRunQuery_block() {
        val result = db.dryRunQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }
        }
        assertNull(result.throwable)
    }
}
