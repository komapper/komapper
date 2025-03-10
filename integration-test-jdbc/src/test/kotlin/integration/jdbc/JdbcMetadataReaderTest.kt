package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.codegen.*
import org.komapper.jdbc.JdbcDatabase
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcMetadataReaderTest(val db: JdbcDatabase) {
    @Test
    fun test() {
        val tables = getTables()
        val address = tables.first { it.name.lowercase() == "address" }
        assertEquals(3, address.columns.size)
    }

    @Run(unless = [Dbms.H2])
    @Test
    fun dump() {
        val tables = getTables()
        val generator = CodeGenerator(null, tables, PackageNameResolver.of(), ClassNameResolver.of("", "", false), PropertyNameResolver.of())
        val writer = StringWriter()
        generator.generateEntities(writer, false, false, false, false, false, PropertyTypeResolver.of(), "", "", "")
        println(writer)
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun dump_h2() {
        val tables = getTables("PUBLIC")
        val generator = CodeGenerator(null, tables, PackageNameResolver.of(), ClassNameResolver.of("", "", false), PropertyNameResolver.of())
        val writer = StringWriter()
        generator.generateEntities(writer, false, false, false, false, false, PropertyTypeResolver.of(), "", "", "")
        println(writer)
    }

    @Run(unless = [Dbms.ORACLE])
    @Test
    fun autoIncrement() {
        val tables = getTables()
        val identityStrategy = tables.first { it.name.lowercase() == "identity_strategy" }
        assertEquals(2, identityStrategy.columns.size)
        val id = identityStrategy.columns.first { it.name.lowercase() == "id" }
        assertTrue(id.isPrimaryKey)
        assertTrue(id.isAutoIncrement)
        val value = identityStrategy.columns.first { it.name.lowercase() == "value" }
        assertFalse(value.isPrimaryKey)
        assertFalse(value.isAutoIncrement)
    }

    private fun getTables(schemaPattern: String? = null): List<Table> {
        return db.config.session.useConnection {
            val reader =
                MetadataReader(db.config.dialect::enquote, it.metaData, null, schemaPattern, null, listOf("TABLE"))
            reader.read()
        }
    }
}
