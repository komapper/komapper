package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntitySelectQuerySelectTest(private val db: Database) {

    @Test
    fun single() {
        val a = Address.alias
        val street = db.execute {
            EntityQuery.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.street).first()
        }
        assertEquals("STREET 1", street)
    }

    @Test
    fun pair() {
        val a = Address.alias
        val (id, street) = db.execute {
            EntityQuery.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.addressId, a.street).first()
        }
        assertEquals(1, id)
        assertEquals("STREET 1", street)
    }
}
