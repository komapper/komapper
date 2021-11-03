package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class EntitySelectQueryForUpdateTest(private val db: R2dbcDatabase) {

    @Test
    fun forUpdate() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            SqlDsl.from(a).where { a.addressId greaterEq 1 }
                .orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
                .forUpdate()
        }.toList()
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
