package org.komapper.codegen

import org.junit.jupiter.api.io.TempDir
import org.komapper.jdbc.dsl.query.MetadataQuery
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Types
import java.util.UUID
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeGeneratorTest {
    @TempDir
    @JvmField
    var tempDir: Path? = null

    @Test
    fun generateEntities() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            destinationDir = destinationDir,
            packageName = "entity",
            tables = createTables()
        )
        generator.generateEntities {
            when (it.typeName.lowercase()) {
                "integer" -> Int::class
                "uuid" -> UUID::class
                else -> String::class
            }
        }
        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity

            data class Address (
                val addressId: Int,
                val street: String?,
                val version: Int,
            )

            data class Employee (
                val employeeId: java.util.UUID,
                val name: String,
                val version: Int,
            )
            
        """.trimIndent()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntities_declareAsNullable() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            destinationDir = destinationDir,
            packageName = "entity",
            tables = createTables()
        )
        generator.generateEntities(declareAsNullable = true) {
            when (it.typeName.lowercase()) {
                "integer" -> Int::class
                "uuid" -> UUID::class
                else -> null
            }
        }
        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity

            data class Address (
                val addressId: Int?,
                val street: String?,
                val version: Int?,
            )

            data class Employee (
                val employeeId: java.util.UUID?,
                val name: String?,
                val version: Int?,
            )
            
        """.trimIndent()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntityDefinition() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            destinationDir = destinationDir,
            packageName = "entity",
            tables = createTables()
        )
        generator.generateDefinitions()
        val file = destinationDir.resolve(Paths.get("entity", "entityDefinitions.kt"))
        val expected = """
            package entity

            import org.komapper.annotation.KomapperAutoIncrement
            import org.komapper.annotation.KomapperColumn
            import org.komapper.annotation.KomapperEntityDef
            import org.komapper.annotation.KomapperId
            import org.komapper.annotation.KomapperTable
            
            @KomapperEntityDef(Address::class)
            @KomapperTable("ADDRESS")
            data class AddressDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("ADDRESS_ID") val addressId: Nothing,
                @KomapperColumn("STREET") val street: Nothing,
                @KomapperColumn("VERSION") val version: Nothing,
            )
            
            @KomapperEntityDef(Employee::class)
            @KomapperTable("EMPLOYEE")
            data class EmployeeDef (
                @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: Nothing,
                @KomapperColumn("NAME") val name: Nothing,
                @KomapperColumn("VERSION") val version: Nothing,
            )
            
        """.trimIndent()
        assertEquals(expected, file.readText())
    }

    private fun createTables(): List<MetadataQuery.Table> {
        return listOf(
            MetadataQuery.Table(
                name = "ADDRESS",
                columns = listOf(
                    MetadataQuery.Column(
                        name = "ADDRESS_ID",
                        dataType = Types.INTEGER,
                        typeName = "integer",
                        isPrimaryKey = true,
                        isAutoIncrement = true
                    ),
                    MetadataQuery.Column(
                        name = "STREET",
                        dataType = Types.VARCHAR,
                        typeName = "varchar",
                        nullable = true
                    ),
                    MetadataQuery.Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
            ),
            MetadataQuery.Table(
                name = "EMPLOYEE",
                columns = listOf(
                    MetadataQuery.Column(
                        name = "EMPLOYEE_ID",
                        dataType = Types.OTHER,
                        typeName = "uuid",
                        isPrimaryKey = true
                    ),
                    MetadataQuery.Column(name = "NAME", dataType = Types.VARCHAR, typeName = "varchar"),
                    MetadataQuery.Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
            )
        )
    }
}
