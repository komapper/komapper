package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class QueryRunnerTest(val db: Database) {

    @Test
    fun list() {
        val address = db.runQuery {
            val a = Address.meta
            EntityDsl.from(a).where { a.addressId eq 1 }
        }
        println(address)
    }

    @Test
    fun first() {
        val address = db.runQuery {
            val a = Address.meta
            EntityDsl.from(a).where { a.addressId eq 1 }.first()
        }
        println(address)
    }

    @Test
    fun firstOrNull() {
        val address = db.runQuery {
            val a = Address.meta
            EntityDsl.from(a).where { a.addressId eq -1 }.firstOrNull()
        }
        println(address)
    }

    @Test
    fun delete() {
        val address = db.runQuery {
            val a = Address.meta
            EntityDsl.from(a).where { a.addressId eq 15 }
        }.first()
        db.runQuery {
            val a = Address.meta
            EntityDsl.delete(a).single(address)
        }
        println(address)
    }
}
