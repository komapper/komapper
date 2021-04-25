package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.desc
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: Database) {

    @Test
    fun list() {
        val a = Address.alias
        val list: List<Address> = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(list)
    }

    @Test
    fun first() {
        val a = Address.alias
        val address: Address = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.alias
        val address: Address? = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling() {
        val a = Address.alias
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

    @Test
    fun shortcut_first() {
        val a = Address.alias
        val address = db.runQuery { EntityDsl.first(a) { a.addressId eq 1 } }
        assertNotNull(address)
    }

    @Test
    fun shortcut_firstOrNull() {
        val a = Address.alias
        val address = db.runQuery { EntityDsl.firstOrNull(a) { a.addressId eq -1 } }
        assertNull(address)
    }

    @Test
    fun shortcut_first_multipleCondition() {
        val a = Address.alias
        val address = db.runQuery {
            EntityDsl.first(a) { a.addressId eq 1; a.version eq 1 }
        }
        assertNotNull(address)
    }
}
