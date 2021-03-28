package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.TemplateQuery

@ExtendWith(Env::class)
class TemplateUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val count = db.execute {
            data class Params(val id: Int, val street: String)

            val sql = "update address set street = /*street*/'' where address_id = /*id*/0"
            TemplateQuery.update(sql, Params(15, "NY street"))
        }
        assertEquals(1, count)
        val a = Address.metamodel()
        val address = db.find(a) {
            a.addressId eq 15
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
