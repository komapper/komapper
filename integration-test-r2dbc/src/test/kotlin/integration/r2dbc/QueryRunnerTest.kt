package integration.r2dbc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class QueryRunnerTest(val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val list = db.execQuery {
            val a = Address.meta
            EntityDsl.from(Address.meta).where { a.addressId eq 1 }
        }
        println(list)
    }

    @Test
    fun delete() = inTransaction(db) {
        val address = db.execQuery {
            val a = Address.meta
            EntityDsl.from(a).where { a.addressId eq 15 }
        }.first()
        db.execQuery {
            val a = Address.meta
            EntityDsl.delete(a).single(address)
        }
    }
//    @Test
//    fun flow() = inTransaction(db) {
//        val flow = db.toFlow {
//            val a = Address.meta
//            EntityDsl.from(Address.meta).where { a.addressId eq 1 }
//        }
//        println(flow.toList())
//    }
}
