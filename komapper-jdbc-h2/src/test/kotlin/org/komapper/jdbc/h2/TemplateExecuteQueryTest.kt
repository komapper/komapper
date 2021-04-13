package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.TemplateQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class TemplateExecuteQueryTest(private val db: Database) {

    @Test
    fun test() {
        val count = db.execute {
            val sql = "update address set street = /*street*/'' where address_id = /*id*/0"
            TemplateQuery.execute(sql).params {
                data class Params(val id: Int, val street: String)
                Params(15, "NY street")
            }
        }
        assertEquals(1, count)
        val a = Address.alias
        val address = db.execute {
            SqlQuery.first(a) {
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
