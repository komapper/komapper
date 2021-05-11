package org.komapper.codegen

import org.komapper.core.SnakeToLowerCamelCase
import org.komapper.core.SnakeToUpperCamelCase
import org.komapper.core.jdbc.Table
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writer

class CodeGenerator(
    private val destinationDir: Path,
    private val packageName: String? = null,
    private val prefix: String = "",
    private val suffix: String = "",
    private val tables: List<Table>
) {

    fun generateEntities(
        fileName: String = "entities.kt",
        overwrite: Boolean = false,
        resolver: ClassResolver
    ) {
        val file = createFilePath(fileName)
        if (Files.exists(file) && !overwrite) {
            return
        }
        mkdirs(file.parent)
        PrintWriter(file.writer()).use { p ->
            if (packageName != null) {
                p.println("package $packageName")
            }
            for (table in tables) {
                p.println()
                val className = SnakeToUpperCamelCase.apply(table.name.name)
                p.println("data class $prefix$className$suffix (")
                for (column in table.columns) {
                    val propertyName = SnakeToLowerCamelCase.apply(column.name)
                    val nullable = if (column.isNullable) "?" else ""
                    val klass = resolver.resolve(column)
                    p.println("    val $propertyName: ${klass.qualifiedName}$nullable,")
                }
                p.println(")")
            }
        }
    }

    fun generateDefinitions(
        fileName: String = "entityDefinitions.kt",
        overwrite: Boolean = false,
        useCatalog: Boolean = false,
        useSchema: Boolean = false,
    ) {
        val file = createFilePath(fileName)
        if (Files.exists(file) && !overwrite) {
            return
        }
        mkdirs(file.parent)
        PrintWriter(file.writer()).use { p ->
            if (packageName != null) {
                p.println("package $packageName")
                p.println()
            }
            p.println("import org.komapper.annotation.KmColumn")
            p.println("import org.komapper.annotation.KmEntityDef")
            p.println("import org.komapper.annotation.KmId")
            p.println("import org.komapper.annotation.KmTable")
            for (table in tables) {
                p.println()
                val tableName = table.name
                val className = SnakeToUpperCamelCase.apply(tableName.name)
                p.println("@KmEntityDef($className::class)")
                val kmTableArgs = listOfNotNull(
                    tableName.name,
                    if (useCatalog) tableName.catalog else null,
                    if (useSchema) tableName.schema else null
                ).joinToString { "\"$it\"" }
                p.println("@KmTable($kmTableArgs)")
                p.println("data class $prefix$className${suffix}Def (")
                for (column in table.columns) {
                    val propertyName = SnakeToLowerCamelCase.apply(column.name)
                    val id = if (column.name in table.primaryKeys) "@KmId " else ""
                    p.println("    $id@KmColumn(\"${column.name}\") val $propertyName: Nothing,")
                }
                p.println(") {")
                p.println("    companion object")
                p.println("}")
            }
        }
    }

    private fun createFilePath(name: String): Path {
        val packageDir = if (packageName == null) {
            destinationDir
        } else {
            val path = packageName.replace(".", "/")
            destinationDir.resolve(path)
        }
        return packageDir.resolve(name)
    }

    private fun mkdirs(dir: Path) {
        Files.createDirectories(dir)
    }
}
