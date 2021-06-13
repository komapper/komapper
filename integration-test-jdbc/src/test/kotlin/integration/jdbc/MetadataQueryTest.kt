package integration.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.dsl.MetadataDsl

@ExtendWith(Env::class)
class MetadataQueryTest(val db: JdbcDatabase) {
    @Test
    fun test() {
        val tables = db.runMetadataQuery {
            MetadataDsl.tables()
        }
        val address = tables.first { it.name.lowercase() == "address" }
        assertEquals(3, address.columns.size)
    }

    @Test
    fun autoIncrement() {
        val tables = db.runMetadataQuery {
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
