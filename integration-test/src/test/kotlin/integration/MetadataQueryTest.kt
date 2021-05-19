package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.MetadataDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class MetadataQueryTest(val db: Database) {

    @Test
    fun test() {
        val tables = db.runQuery {
            MetadataDsl.tables()
        }
        val address = tables.first { it.name.lowercase() == "address" }
        assertEquals(3, address.columns.size)
    }

    @Test
    fun autoIncrement() {
        val tables = db.runQuery {
            MetadataDsl.tables()
        }
        val identityStrategy = tables.first { it.name.lowercase() == "identity_strategy" }
        assertEquals(2, identityStrategy.columns.size)
        val id = identityStrategy.columns.first { it.name.lowercase() == "id" }
        assertTrue(id.isPrimaryKey)
        assertTrue(id.isAutoIncrement)
        val value = identityStrategy.columns.first { it.name.lowercase() == "value" }
        assertFalse(value.isPrimaryKey)
        assertFalse(value.isAutoIncrement)
    }
}
