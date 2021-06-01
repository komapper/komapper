package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl
import org.komapper.r2dbc.dsl.R2dbcTemplateDsl

@ExtendWith(Env::class)
class TemplateExecuteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val count = db.runQuery {
            val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
            R2dbcTemplateDsl.execute(sql).params {
                data class Params(val id: Int, val street: String)
                Params(15, "NY street")
            }
        }
        assertEquals(1, count)
        val a = Address.meta
        val address = db.runQuery {
            R2dbcSqlDsl.from(a).first {
                a.addressId eq 15
            }
        }
        assertEquals(
            Address(
                15,
                "NY street",
                1
            ),
            address
        )
    }
}
