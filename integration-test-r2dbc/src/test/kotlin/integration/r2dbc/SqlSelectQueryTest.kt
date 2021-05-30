package integration.r2dbc

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl
import org.komapper.tx.r2dbc.transaction

@ExtendWith(Env::class)
class SqlSelectQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun where() = runBlocking {
        db.transaction {
            setRollbackOnly()
            val flow = db.runQuery {
                val a = Address.meta
                R2dbcSqlDsl.from(Address.meta)
                    .where { a.addressId inList listOf(1, 2, 3) }
                    .orderBy(a.addressId)
            }
            val list = flow.toList(mutableListOf())
            Assertions.assertEquals(listOf(1, 2, 3), list.map { it.addressId })
        }
    }
}
