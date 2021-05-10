package org.komapper.codegen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.komapper.core.jdbc.Column
import org.komapper.core.jdbc.Table
import org.komapper.core.jdbc.TableName
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Types
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
                else -> String::class
            }
        }
        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity

            data class Address (
                val addressId: kotlin.Int,
                val street: kotlin.String?,
                val version: kotlin.Int,
            )

            data class Employee (
                val employeeId: kotlin.Int,
                val name: kotlin.String,
                val version: kotlin.Int,
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

            import org.komapper.annotation.KmEntityDef
            import org.komapper.annotation.KmId
            import org.komapper.annotation.KmTable
            
            @KmEntityDef(Address::class)
            @KmTable("ADDRESS")
            data class AddressDef (
                @KmId val addressId: Nothing,
                val street: Nothing,
                val version: Nothing,
            ) {
                companion object
            }
            
            @KmEntityDef(Employee::class)
            @KmTable("EMPLOYEE")
            data class EmployeeDef (
                @KmId val employeeId: Nothing,
                val name: Nothing,
                val version: Nothing,
            ) {
                companion object
            }
            
        """.trimIndent()
        assertEquals(expected, file.readText())
    }

    private fun createTables(): List<Table> {
        return listOf(
            Table(
                TableName("ADDRESS"),
                listOf(
                    Column(name = "ADDRESS_ID", dataType = Types.INTEGER, typeName = "integer"),
                    Column(name = "STREET", dataType = Types.VARCHAR, typeName = "varchar", isNullable = true),
                    Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
                listOf("ADDRESS_ID")
            ),
            Table(
                TableName("EMPLOYEE"),
                listOf(
                    Column(name = "EMPLOYEE_ID", dataType = Types.INTEGER, typeName = "integer"),
                    Column(name = "NAME", dataType = Types.VARCHAR, typeName = "varchar"),
                    Column(name = "VERSION", dataType = Types.INTEGER, typeName = "integer"),
                ),
                listOf("EMPLOYEE_ID")
            )
        )
    }
}
