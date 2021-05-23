package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.EntityDsl
import org.komapper.jdbc.dsl.runQuery

@ExtendWith(Env::class)
class EntitySelectQuerySelectTest(private val db: Database) {

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
