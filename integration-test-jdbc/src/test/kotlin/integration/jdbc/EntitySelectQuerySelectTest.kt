package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class EntitySelectQuerySelectTest(private val db: JdbcDatabase) {

    @Test
    fun single() {
        val a = Address.meta
        val street = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.street).first()
        }
        assertEquals("STREET 1", street)
    }

    @Test
    fun singleList() {
        val a = Address.meta
        val streets = db.runQuery {
            EntityDsl.from(a).where { a.addressId inList listOf(1, 2) }
                .asSqlQuery().select(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streets)
    }

    @Test
    fun pair() {
        val a = Address.meta
        val (id, street) = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.addressId, a.street).first()
        }
        assertEquals(1, id)
        assertEquals("STREET 1", street)
    }
}
