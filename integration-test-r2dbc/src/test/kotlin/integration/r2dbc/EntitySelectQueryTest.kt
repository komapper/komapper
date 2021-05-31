package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.desc
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun list() = inTransaction(db) {
        val a = Address.meta
        val flow = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(flow)
    }

    @Test
    fun first() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address: Address? = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling() = inTransaction(db) {
        val a = Address.meta
        val query = R2dbcEntityDsl.from(a)
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

    @Test
    fun shortcut_first() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { R2dbcEntityDsl.from(a).first { a.addressId eq 1 } }
        assertNotNull(address)
    }

    @Test
    fun shortcut_firstOrNull() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { R2dbcEntityDsl.from(a).firstOrNull { a.addressId eq -1 } }
        assertNull(address)
    }

    @Test
    fun shortcut_first_multipleCondition() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery {
            R2dbcEntityDsl.from(a).first { a.addressId eq 1; a.version eq 1 }
        }
        assertNotNull(address)
    }
}
