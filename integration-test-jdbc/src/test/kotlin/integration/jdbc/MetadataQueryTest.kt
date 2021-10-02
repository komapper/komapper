package integration.jdbc

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.dsl.MetadataDsl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class MetadataQueryTest(val db: JdbcDatabase) {
    @Test
    fun test() {
        val tables = MetadataDsl.tables().run(db.config)
        val address = tables.first { it.name.lowercase() == "address" }
        assertEquals(3, address.columns.size)
    }

    @Test
    fun autoIncrement() {
        val tables = MetadataDsl.tables().run(db.config)
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
