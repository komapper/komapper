package org.komapper.codegen

import org.junit.jupiter.api.io.TempDir
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
            "entity",
            createTables(),
            ClassNameResolver.of("", "", false),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entities.kt", false).use { writer ->
            generator.generateEntities(writer, false, false, false, false, false, DummyPropertyTypeResolver(), "", "", "")
        }

        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity
            
            data class Address (
                val addressId: Int,
                val street: String?,
                val version: Int,
                val createdAt: java.time.LocalDateTime,
                val updatedAt: java.time.LocalDateTime,
            )
            
            data class Employee (
                val employeeId: java.util.UUID,
                val name: String,
                val version: Int,
            )
            
            data class Class (
                val classId: Int,
                val `super`: String?,
                val `val`: String,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntities_declareAsNullable() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTables(),
            ClassNameResolver.of("", "", false),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entities.kt", false).use { writer ->
            generator.generateEntities(writer, true, false, false, false, false, DummyPropertyTypeResolver(), "", "", "")
        }
        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity
            
            data class Address (
                val addressId: Int?,
                val street: String?,
                val version: Int?,
                val createdAt: java.time.LocalDateTime?,
                val updatedAt: java.time.LocalDateTime?,
            )
            
            data class Employee (
                val employeeId: java.util.UUID?,
                val name: String?,
                val version: Int?,
            )
            
            data class Class (
                val classId: Int?,
                val `super`: String?,
                val `val`: String?,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntities_useSelfMapping() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTables(),
            ClassNameResolver.of("", "", false),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entities.kt", false).use { writer ->
            generator.generateEntities(writer, false, true, false, false, false, DummyPropertyTypeResolver(), "", "", "")
        }
        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity
            
            import org.komapper.annotation.KomapperAutoIncrement
            import org.komapper.annotation.KomapperColumn
            import org.komapper.annotation.KomapperEntity
            import org.komapper.annotation.KomapperId
            import org.komapper.annotation.KomapperTable
            
            @KomapperEntity
            @KomapperTable("ADDRESS")
            data class Address (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("ADDRESS_ID") val addressId: Int,
                @KomapperColumn("STREET") val street: String?,
                @KomapperColumn("VERSION") val version: Int,
                @KomapperColumn("CREATED_AT") val createdAt: java.time.LocalDateTime,
                @KomapperColumn("UPDATED_AT") val updatedAt: java.time.LocalDateTime,
            )
            
            @KomapperEntity
            @KomapperTable("EMPLOYEE")
            data class Employee (
                @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: java.util.UUID,
                @KomapperColumn("NAME") val name: String,
                @KomapperColumn("VERSION") val version: Int,
            )
            
            @KomapperEntity
            @KomapperTable("CLASS")
            data class Class (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("CLASS_ID") val classId: Int,
                @KomapperColumn("SUPER") val `super`: String?,
                @KomapperColumn("VAL") val `val`: String,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntities_singularize() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTablesWithPluralNames(),
            ClassNameResolver.of("", "", true),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entities.kt", false).use { writer ->
            generator.generateEntities(writer, false, false, true, false, false, DummyPropertyTypeResolver(), "", "", "")
        }

        val file = destinationDir.resolve(Paths.get("entity", "entities.kt"))
        val expected = """
            package entity
            
            data class Address (
                val addressId: Int,
                val street: String?,
                val version: Int,
                val createdAt: java.time.LocalDateTime,
                val updatedAt: java.time.LocalDateTime,
            )
            
            data class Employee (
                val employeeId: java.util.UUID,
                val name: String,
                val version: Int,
            )
            
            data class Class (
                val classId: Int,
                val `super`: String?,
                val `val`: String,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntityDefinition() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTables(),
            ClassNameResolver.of("", "", false),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entityDefinitions.kt", false).use { writer ->
            generator.generateDefinitions(writer, false, false, false, "", "", "")
        }
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
                @KomapperColumn("CREATED_AT") val createdAt: Nothing,
                @KomapperColumn("UPDATED_AT") val updatedAt: Nothing,
            )
            
            @KomapperEntityDef(Employee::class)
            @KomapperTable("EMPLOYEE")
            data class EmployeeDef (
                @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: Nothing,
                @KomapperColumn("NAME") val name: Nothing,
                @KomapperColumn("VERSION") val version: Nothing,
            )
            
            @KomapperEntityDef(Class::class)
            @KomapperTable("CLASS")
            data class ClassDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("CLASS_ID") val classId: Nothing,
                @KomapperColumn("SUPER") val `super`: Nothing,
                @KomapperColumn("VAL") val `val`: Nothing,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntityDefinition_moreAnnotations() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTables(),
            ClassNameResolver.of("", "", false),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entityDefinitions.kt", false).use { writer ->
            generator.generateDefinitions(writer, false, false, false, "version", "createdAt", "updatedAt")
        }
        val file = destinationDir.resolve(Paths.get("entity", "entityDefinitions.kt"))
        val expected = """
            package entity
            
            import org.komapper.annotation.KomapperAutoIncrement
            import org.komapper.annotation.KomapperColumn
            import org.komapper.annotation.KomapperCreatedAt
            import org.komapper.annotation.KomapperEntityDef
            import org.komapper.annotation.KomapperId
            import org.komapper.annotation.KomapperTable
            import org.komapper.annotation.KomapperUpdatedAt
            import org.komapper.annotation.KomapperVersion
            
            @KomapperEntityDef(Address::class)
            @KomapperTable("ADDRESS")
            data class AddressDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("ADDRESS_ID") val addressId: Nothing,
                @KomapperColumn("STREET") val street: Nothing,
                @KomapperVersion @KomapperColumn("VERSION") val version: Nothing,
                @KomapperCreatedAt @KomapperColumn("CREATED_AT") val createdAt: Nothing,
                @KomapperUpdatedAt @KomapperColumn("UPDATED_AT") val updatedAt: Nothing,
            )
            
            @KomapperEntityDef(Employee::class)
            @KomapperTable("EMPLOYEE")
            data class EmployeeDef (
                @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: Nothing,
                @KomapperColumn("NAME") val name: Nothing,
                @KomapperVersion @KomapperColumn("VERSION") val version: Nothing,
            )
            
            @KomapperEntityDef(Class::class)
            @KomapperTable("CLASS")
            data class ClassDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("CLASS_ID") val classId: Nothing,
                @KomapperColumn("SUPER") val `super`: Nothing,
                @KomapperColumn("VAL") val `val`: Nothing,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    @Test
    fun generateEntityDefinition_singularize() {
        val destinationDir = tempDir!!.resolve(Paths.get("src", "kotlin", "main"))
        val generator = CodeGenerator(
            "entity",
            createTablesWithPluralNames(),
            ClassNameResolver.of("", "", true),
            PropertyNameResolver.of()
        )
        generator.createNewFile(destinationDir, "entityDefinitions.kt", false).use { writer ->
            generator.generateDefinitions(writer, true, false, false, "", "", "")
        }
        val file = destinationDir.resolve(Paths.get("entity", "entityDefinitions.kt"))
        val expected = """
            package entity
            
            import org.komapper.annotation.KomapperAutoIncrement
            import org.komapper.annotation.KomapperColumn
            import org.komapper.annotation.KomapperEntityDef
            import org.komapper.annotation.KomapperId
            import org.komapper.annotation.KomapperTable
            
            @KomapperEntityDef(Address::class, ["addresses"])
            @KomapperTable("ADDRESSES")
            data class AddressDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("ADDRESS_ID") val addressId: Nothing,
                @KomapperColumn("STREET") val street: Nothing,
                @KomapperColumn("VERSION") val version: Nothing,
                @KomapperColumn("CREATED_AT") val createdAt: Nothing,
                @KomapperColumn("UPDATED_AT") val updatedAt: Nothing,
            )
            
            @KomapperEntityDef(Employee::class, ["employees"])
            @KomapperTable("EMPLOYEES")
            data class EmployeeDef (
                @KomapperId @KomapperColumn("EMPLOYEE_ID") val employeeId: Nothing,
                @KomapperColumn("NAME") val name: Nothing,
                @KomapperColumn("VERSION") val version: Nothing,
            )
            
            @KomapperEntityDef(Class::class, ["classes"])
            @KomapperTable("CLASSES")
            data class ClassDef (
                @KomapperId @KomapperAutoIncrement @KomapperColumn("CLASS_ID") val classId: Nothing,
                @KomapperColumn("SUPER") val `super`: Nothing,
                @KomapperColumn("VAL") val `val`: Nothing,
            )
            
        """.trimIndent().normalizeLineSeparator()
        assertEquals(expected, file.readText())
    }

    private fun createTables(): List<MutableTable> {
        return listOf(
            MutableTable().apply {
                name = "ADDRESS"
                columns = listOf(
                    MutableColumn().apply {
                        name = "ADDRESS_ID"
                        dataType = Types.INTEGER
                        typeName = "integer"
                        isPrimaryKey = true
                        isAutoIncrement = true
                    },
                    MutableColumn().apply {
                        name = "STREET"
                        dataType = Types.VARCHAR
                        typeName = "varchar"
                        nullable = true
                    },
                    MutableColumn().apply {
                        name = "VERSION"
                        dataType = Types.INTEGER
                        typeName = "integer"
                    },
                    MutableColumn().apply {
                        name = "CREATED_AT"
                        dataType = Types.TIMESTAMP
                        typeName = "timestamp"
                    },
                    MutableColumn().apply {
                        name = "UPDATED_AT"
                        dataType = Types.TIMESTAMP
                        typeName = "timestamp"
                    }
                )
            },
            MutableTable().apply {
                name = "EMPLOYEE"
                columns = listOf(
                    MutableColumn().apply {
                        name = "EMPLOYEE_ID"
                        dataType = Types.OTHER
                        typeName = "uuid"
                        isPrimaryKey = true
                    },
                    MutableColumn().apply {
                        name = "NAME"
                        dataType = Types.VARCHAR
                        typeName = "varchar"
                    },
                    MutableColumn().apply {
                        name = "VERSION"
                        dataType = Types.INTEGER
                        typeName = "integer"
                    }
                )
            },
            MutableTable().apply {
                name = "CLASS"
                columns = listOf(
                    MutableColumn().apply {
                        name = "CLASS_ID"
                        dataType = Types.INTEGER
                        typeName = "integer"
                        isPrimaryKey = true
                        isAutoIncrement = true
                    },
                    MutableColumn().apply {
                        name = "SUPER"
                        dataType = Types.VARCHAR
                        typeName = "varchar"
                        nullable = true
                    },
                    MutableColumn().apply {
                        name = "VAL"
                        dataType = Types.VARCHAR
                        typeName = "varchar"
                    }
                )
            }
        )
    }

    private fun createTablesWithPluralNames(): List<MutableTable> {
        val tables = createTables()
        tables[0].name = "ADDRESSES"
        tables[1].name = "EMPLOYEES"
        tables[2].name = "CLASSES"
        return tables;
    }

    private fun String.normalizeLineSeparator(): String =
        replace(Regex("\r?\n"), System.lineSeparator())

    private class DummyPropertyTypeResolver : PropertyTypeResolver {
        override fun resolve(table: Table, column: Column): String {
            return when (column.typeName.lowercase()) {
                "integer" -> Int::class.simpleName.toString()
                "uuid" -> UUID::class.qualifiedName.toString()
                "timestamp" -> java.time.LocalDateTime::class.qualifiedName.toString()
                else -> String::class.simpleName.toString()
            }
        }
    }
}
