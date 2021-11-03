package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun list() = inTransaction(db) {
        val a = Address.meta
        val flow = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(flow)
    }

    @Test
    fun first() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address: Address? = db.runQuery {
            SqlDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling() = inTransaction(db) {
        val a = Address.meta
        val query = SqlDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val flow = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            flow.toList()
        )
    }
}
