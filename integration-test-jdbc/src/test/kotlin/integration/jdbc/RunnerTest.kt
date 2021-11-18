package integration.jdbc

import integration.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(Env::class)
class RunnerTest(val db: JdbcDatabase) {

    @Test
    fun list() {
        val address = db.runQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }
        }
        println(address)
    }

    @Test
    fun first() {
        val address = db.runQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }.first()
        }
        println(address)
    }

    @Test
    fun firstOrNull() {
        val address = db.runQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq -1 }.firstOrNull()
        }
        println(address)
    }

    @Test
    fun delete() {
        val address = db.runQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 15 }
        }.first()
        db.runQuery {
            val a = Meta.address
            QueryDsl.delete(a).single(address)
        }
        println(address)
    }
}
