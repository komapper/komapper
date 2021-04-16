package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.desc
import org.komapper.core.dsl.execute
import org.komapper.core.dsl.scope.WhereScope.Companion.plus

@ExtendWith(Env::class)
class EntitySelectQueryForUpdateTest(private val db: Database) {

    @Test
    fun forUpdate() {
        val a = Address.alias
        val list = db.execute {
            EntityQuery.from(a).where { a.addressId greaterEq 1 }
                .orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
