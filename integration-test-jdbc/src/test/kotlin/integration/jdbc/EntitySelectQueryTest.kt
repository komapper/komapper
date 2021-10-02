package integration.jdbc

import integration.Address
import integration.meta
import kotlinx.coroutines.flow.count
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: JdbcDatabase) {

    @Test
    fun list() {
        val a = Address.meta
        val list: List<Address> = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(list)
    }

    @Test
    fun first() {
        val a = Address.meta
        val address: Address = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.meta
        val address: Address? = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun collect() {
        val a = Address.meta
        val count = db.runQuery {
            EntityDsl.from(a).collect { it.count() }
        }
        assertEquals(15, count)
    }

    @Test
    fun decoupling() {
        val a = Address.meta
        val query = EntityDsl.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val list = db.runQuery { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
