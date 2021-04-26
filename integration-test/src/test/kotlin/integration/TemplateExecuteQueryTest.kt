package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class TemplateExecuteQueryTest(private val db: Database) {

    @Test
    fun test() {
        val count = db.runQuery {
            val sql = "update ADDRESS set street = /*street*/'' where address_id = /*id*/0"
            TemplateDsl.execute(sql).params {
                data class Params(val id: Int, val street: String)
                Params(15, "NY street")
            }
        }
        assertEquals(1, count)
        val a = Address.alias
        val address = db.runQuery {
            SqlDsl.from(a).first {
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
