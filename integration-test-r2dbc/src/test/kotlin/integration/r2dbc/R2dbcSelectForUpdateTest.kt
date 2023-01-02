package integration.r2dbc

import integration.core.Address
import integration.core.address
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcSelectForUpdateTest(private val db: R2dbcDatabase) {

    @Test
    fun forUpdate(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(9, 10) }
                .orderBy(a.addressId.desc())
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1),
            ),
            list,
        )
    }
}
