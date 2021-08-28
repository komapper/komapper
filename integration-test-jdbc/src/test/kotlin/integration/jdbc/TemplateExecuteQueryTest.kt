package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.jdbc.JdbcDatabase

@ExtendWith(Env::class)
class TemplateExecuteQueryTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val count = db.runQuery {
            val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
            TemplateDsl.execute(sql).bind {
                data class Params(val id: Int, val street: String)
                Params(15, "NY street")
            }
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
