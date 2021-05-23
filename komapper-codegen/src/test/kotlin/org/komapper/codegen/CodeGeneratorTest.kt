package org.komapper.codegen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.komapper.core.Column
import org.komapper.core.Table
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Types
import java.util.UUID
import kotlin.io.path.readText

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

            import org.komapper.annotation.*
            
            @KmEntityDef(Address::class)
            @KmTable("ADDRESS")
            data class AddressDef (
                @KmId @KmAutoIncrement @KmColumn("ADDRESS_ID") val addressId: Nothing,
                @KmColumn("STREET") val street: Nothing,
                @KmColumn("VERSION") val version: Nothing,
            ) {
                companion object
            }
            
            @KmEntityDef(Employee::class)
            @KmTable("EMPLOYEE")
            data class EmployeeDef (
                @KmId @KmColumn("EMPLOYEE_ID") val employeeId: Nothing,
                @KmColumn("NAME") val name: Nothing,
                @KmColumn("VERSION") val version: Nothing,
            ) {
                companion object
            }
            
        """.trimIndent()
        assertEquals(expected, file.readText())
    }

    private fun createTables(): List<Table> {
        return listOf(
            Table(
                name = "ADDRESS",
                columns = listOf(
                    Column(name = "ADDRESS_ID", dataType = Types.INTEGER, typeName = "integer", isPrimaryKey = true, isAutoIncrement = true),
                    Column(name = "STREET", dataType = Types.VARCHAR, typeName = "varchar", nullable = true),
                    Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
            ),
            Table(
                name = "EMPLOYEE",
                columns = listOf(
                    Column(name = "EMPLOYEE_ID", dataType = Types.OTHER, typeName = "uuid", isPrimaryKey = true),
                    Column(name = "NAME", dataType = Types.VARCHAR, typeName = "varchar"),
                    Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
            )
        )
    }
}
