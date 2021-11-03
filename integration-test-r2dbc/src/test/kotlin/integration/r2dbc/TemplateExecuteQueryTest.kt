package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class TemplateExecuteQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val count = db.runQuery {
            data class Condition(val id: Int, val street: String)

            val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
            TemplateDsl.execute(sql).bind(Condition(15, "NY street"))
        }
        assertEquals(1, count)
        val a = Address.meta
        val address = db.runQuery {
            SqlDsl.from(a).where {
                a.addressId eq 15
            }.first()
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
