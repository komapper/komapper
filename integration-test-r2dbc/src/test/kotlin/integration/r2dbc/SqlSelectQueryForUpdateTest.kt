package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.desc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl

@ExtendWith(Env::class)
class SqlSelectQueryForUpdateTest(private val db: R2dbcDatabase) {

    @Test
    fun forUpdate() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            R2dbcSqlDsl.from(a).where { a.addressId greaterEq 1 }
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
