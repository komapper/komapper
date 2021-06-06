package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class FlowableQueryTest(val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val flow = db.runFlowableQuery {
            val a = Address.meta
            SqlDsl.from(Address.meta).where { a.addressId eq 1 }
        }
        println(flow)
    }
}
